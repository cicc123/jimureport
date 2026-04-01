package com.jimureport.enhancement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * JimuReport增强版启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.jimureport.enhancement", "org.jeecg.modules.jmreport"})
@MapperScan("com.jimureport.enhancement.mapper")
public class JimuReportEnhancementApplication {

    public static void main(String[] args) {
        SpringApplication.run(JimuReportEnhancementApplication.class, args);
        System.out.println("\n" +
                "  _____  _                  ____                       _                   \n" +
                " |_   _|| |__    ___  _ __ |  _ \\  _ __  ___   _ __  | |__    ___  _ __  \n" +
                "   | |  | '_ \\ / _ \\| '_ \\| |_) || '__|/ _ \\ | '_ \\ | '_ \\  / _ \\| '__| \n" +
                "   | |  | | | |  __/| | | |  __/ | |  | (_) || | | || | | ||  __/| |    \n" +
                "   |_|  |_| |_|\\___||_| |_|_|    |_|   \\___/ |_| |_||_| |_| \\___||_|    \n" +
                "                                                                         \n" +
                "  增强版启动成功！\n" +
                "  API文档: http://localhost:8085/swagger-ui.html\n" +
                "  积木报表: http://localhost:8085/jmreport/list\n" +
                "  默认账号: admin / admin123\n");
    }
}
