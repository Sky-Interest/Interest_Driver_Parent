package com.gec.interest.rules.utils;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

public class DroolsHelper {

    private static final String RULES_CUSTOMER_RULES_DRL = "rules/FeeRule.drl";

    public static KieSession loadForRule(String drlStr) {
        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        //ResourceFactory 是一个工厂类，用于创建 Drools 资源。newClassPathResource() 方法用于从类路径中加载资源文件（如 .drl 文件）。
        //解析参数：
        //drlStr：这个参数是一个字符串，表示 .drl 文件在类路径中的位置。
        // 例如，drlStr 可能是 "rules/sample.drl"，表示这个文件位于 src/main/resources/rules/sample.drl 中。
        kieFileSystem.write(
                ResourceFactory.newClassPathResource(drlStr));

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();

        KieModule kieModule = kb.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        return kieContainer.newKieSession();
    }
}
