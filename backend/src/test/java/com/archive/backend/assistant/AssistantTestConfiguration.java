package com.archive.backend.assistant;

import com.archive.backend.assistant.web.chat.RunLauncher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * The assistant of the integration tests: a scripted model in place of OpenAI, and runs made at once in the thread
 * of the request, inside the test's transaction, so a test sees all they did when the request returns.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AssistantTestConfiguration {

    /** The model every use of the language model gets. */
    @Bean
    @Primary
    ScriptedLanguageModel scriptedLanguageModel() {
        return new ScriptedLanguageModel();
    }

    @Bean
    @Primary
    RunLauncher runsAtOnce() {
        return Runnable::run;
    }
}
