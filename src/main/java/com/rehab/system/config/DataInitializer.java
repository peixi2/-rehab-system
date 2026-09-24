package com.rehab.system.config;

import com.rehab.system.common.RoleConstants;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfAbsent("admin", "123456", RoleConstants.ADMIN, "系统管理员", null);
        createIfAbsent("doctor", "123456", RoleConstants.DOCTOR, "张建国", 1L);
        createIfAbsent("therapist", "123456", RoleConstants.THERAPIST, "刘洋", 5L);
        createIfAbsent("deptadmin", "123456", RoleConstants.DEPT_ADMIN, "科室管理员", null);
        createIfAbsent("patient", "123456", RoleConstants.PATIENT, "赵国强", null);
        bindPatientAccountsByRealName();
        log.info("默认账号初始化完成");
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

    private void createIfAbsent(String username, String rawPassword, String role, String realName, Long doctorId) {
        if (!userRepository.existsByUsername(username)) {
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setRealName(realName);
            user.setDoctorId(doctorId);
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}
