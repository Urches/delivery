package microarch.delivery.config;

import microarch.delivery.adapters.in.http.jobs.AssignOrderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Quartz для фоновых задач.
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail assignOrderJobDetail() {
        return JobBuilder.newJob(AssignOrderJob.class).withIdentity("assignOrderJob").storeDurably().build();
    }

    @Bean
    public Trigger assignOrderTrigger(JobDetail assignOrderJobDetail) {
        return TriggerBuilder.newTrigger().forJob(assignOrderJobDetail).withIdentity("assignOrderTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1) // каждую 1 секунду
                        .repeatForever())
                .build();
    }
}
