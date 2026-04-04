package com.jettech.api.solutions_clinic.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Garante que o Flyway execute as migrations antes do Hibernate validar o schema.
 * O EntityManagerFactory passa a depender do bean que dispara as migrations do Flyway,
 * independente do nome que o Spring Boot 4 use (busca pelo tipo, não pelo nome).
 */
@Configuration
public class FlywayBeforeJpaConfig implements BeanFactoryPostProcessor, Ordered {

    private static final String ENTITY_MANAGER_FACTORY_BEAN = "entityManagerFactory";
    private static final String FLYWAY_INITIALIZER_CLASS_SUBSTRING = "FlywayMigrationInitializer";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            return;
        }
        if (!registry.containsBeanDefinition(ENTITY_MANAGER_FACTORY_BEAN)) {
            return;
        }

        String flywayInitializerBeanName = null;
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition def = registry.getBeanDefinition(beanName);
            String className = def.getBeanClassName();
            if (className != null && className.contains(FLYWAY_INITIALIZER_CLASS_SUBSTRING)) {
                flywayInitializerBeanName = beanName;
                break;
            }
        }

        if (flywayInitializerBeanName == null) {
            return;
        }

        BeanDefinition emfDef = registry.getBeanDefinition(ENTITY_MANAGER_FACTORY_BEAN);
        String[] currentDependsOn = emfDef.getDependsOn();
        String newDependsOn = flywayInitializerBeanName;
        if (currentDependsOn != null && currentDependsOn.length > 0) {
            newDependsOn = flywayInitializerBeanName + "," + String.join(",", currentDependsOn);
        }
        emfDef.setDependsOn(newDependsOn.split(","));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
