package com.whd.cube;

import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext; // 1. 引入 UserContext
import com.whd.cube.controller.RegistrationController;
import com.whd.cube.dto.RegistrationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@SpringBootTest
@Transactional
public class RegistrationTest {

    @Autowired
    private RegistrationController registrationController;

    // 每个测试跑完后清理 ThreadLocal，防止影响其他测试
    @AfterEach
    public void cleanUp() {
        UserContext.remove();
    }

    @Test
    public void testApplyExpiredCompetition() {
        System.out.println("========== 开始测试：给已结束的比赛报名 ==========");

        // 1. 准备数据
        Long competitionId = 5L; // 2023广州夏季赛 (已结束)
        Long userId = 1002L;     // 张三

        // ★★★ 核心修改：手动模拟拦截器行为，设置当前用户 ★★★
        UserContext.setUserId(userId);

        RegistrationDTO dto = new RegistrationDTO();
        dto.setCompetitionId(competitionId);
        dto.setEventIds(Arrays.asList("333", "222"));

        // 2. 执行报名 (现在的 apply 不需要传 userId 了)
        Result result = registrationController.apply(dto);

        // 3. 打印结果
        System.out.println("返回代码: " + result.getCode());
        System.out.println("返回消息: " + result.getMsg());

        // 4. 验证结果
        Assertions.assertEquals(500, result.getCode(), "预期报名应该失败，但却成功了！");

        boolean isErrorMsgCorrect = result.getMsg().contains("未开启") || result.getMsg().contains("截止");
        Assertions.assertTrue(isErrorMsgCorrect, "错误信息不符合预期，实际信息是：" + result.getMsg());

        System.out.println("========== 测试通过：成功拦截了非法报名！ ==========");
    }

    @Test
    public void testApplyFutureCompetition() {
        System.out.println("\n========== 开始测试：给正常的比赛报名 ==========");

        // 1. 准备数据
        Long competitionId = 4L; // 2026上海新年赛 (未来)
        Long userId = 1002L;     // 张三

        // ★★★ 核心修改：手动模拟拦截器行为 ★★★
        UserContext.setUserId(userId);

        RegistrationDTO dto = new RegistrationDTO();
        dto.setCompetitionId(competitionId);
        dto.setEventIds(Arrays.asList("333"));

        // 2. 执行报名 (现在的 apply 不需要传 userId 了)
        Result result = registrationController.apply(dto);

        System.out.println("返回消息: " + result.getMsg());

        // 3. 验证结果
        Assertions.assertEquals(200, result.getCode());
        System.out.println("========== 测试通过：正常报名成功！ ==========");
    }
}