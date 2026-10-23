package com.rehab.system.config;

import com.rehab.system.common.RoleConstants;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("admin", RoleConstants.ADMIN, "系统管理员", null),
            new DemoAccount("doctor", RoleConstants.DOCTOR, "张建国", 1L),
            new DemoAccount("therapist", RoleConstants.THERAPIST, "刘洋", 5L),
            new DemoAccount("deptadmin", RoleConstants.DEPT_ADMIN, "科室管理员", null),
            new DemoAccount("patient", RoleConstants.PATIENT, "赵国强", null)
    );

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DataInitializer(UserRepository userRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.demo.password}") String demoPassword) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        String encodedPassword = passwordEncoder.encode(demoPassword);
        DEMO_ACCOUNTS.forEach(account -> upsertDemoAccount(account, encodedPassword));
        bindPatientAccountsByRealName();
        log.info("演示账号初始化及密码同步完成");
    }

    /** 患者账号未绑定病人时，按姓名自动关联同名病人 */
    private void bindPatientAccountsByRealName() {
        userRepository.findAll().stream()
                .filter(u -> RoleConstants.PATIENT.equals(u.getRole()) && u.getPatientId() == null && u.getRealName() != null)
                .forEach(u -> patientRepository.findByName(u.getRealName())
                        .ifPresent(p -> {
                            u.setPatientId(p.getId());
                            userRepository.save(u);
                            log.info("患者账号 {} 已自动绑定病人 {} (ID={})", u.getUsername(), p.getName(), p.getId());
                        }));
    }

    private void upsertDemoAccount(DemoAccount account, String encodedPassword) {
        SysUser user = userRepository.findByUsername(account.username()).orElseGet(() -> {
            SysUser created = new SysUser();
            created.setUsername(account.username());
            created.setRole(account.role());
            created.setRealName(account.realName());
            created.setDoctorId(account.doctorId());
            created.setEnabled(true);
            return created;
        });
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private record DemoAccount(String username, String role, String realName, Long doctorId) {
    }
}
