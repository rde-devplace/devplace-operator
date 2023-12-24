package com.mibottle.ideoperators.service;

import com.mibottle.ideoperators.customresource.IdeConfig;
import com.mibottle.ideoperators.customresource.IdeConfigSpec;
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class IdeConfigService {

    private final KubernetesClient client;

    public static final class IdeConfigList extends DefaultKubernetesResourceList<IdeConfig> {
    }
    public IdeConfigService(KubernetesClient client) {
        this.client = client;
    }

    public IdeConfig createIdeConfig(String namespace, String ideConfigName, IdeConfigSpec ideConfigSpec) {
        log.info("Creating IdeConfig: " + ideConfigSpec.toString());
        NonNamespaceOperation<IdeConfig, IdeConfigList, Resource<IdeConfig>> ideConfigs =
                client.resources(IdeConfig.class, IdeConfigList.class).inNamespace(namespace);

        // Check if any field of ideConfigSpec is null
        boolean hasNullField = Arrays.stream(ideConfigSpec.getClass().getDeclaredFields())
                .anyMatch(field -> {
                    field.setAccessible(true); // in case the field is private
                    try {
                        return field.get(ideConfigSpec) == null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return false;
                    }
                });

        IdeConfig ideConfig = new IdeConfig(ideConfigName, ideConfigSpec);
        if (!hasNullField) {
            ideConfigs.resource(ideConfig).create();
        } else {
            log.warn("IdeConfigSpec has a null field. Skipping ideConfig creation.");
        }

        ideConfigs.list().getItems()
                .forEach(s -> System.out.printf(" - %s%n", s.getSpec().getUserName()));

        return ideConfig;
    }

    public IdeConfig deleteIdeConfig(String namespace, String ideConfigName) {
        NonNamespaceOperation<IdeConfig, IdeConfigList, Resource<IdeConfig>> ideConfigs =
                client.resources(IdeConfig.class, IdeConfigList.class).inNamespace(namespace);

        IdeConfig ideConfig = new IdeConfig(ideConfigName, null);
        ideConfigs.resource(ideConfig).delete();
        ideConfigs.list().getItems()
                .forEach(s -> System.out.printf(" - %s%n", s.getSpec().getUserName()));

        return ideConfig;
    }

    private List<IdeConfig> fetchIdeConfigs(String namespace) {
        NonNamespaceOperation<IdeConfig, IdeConfigList, Resource<IdeConfig>> ideConfigs =
                client.resources(IdeConfig.class, IdeConfigList.class).inNamespace(namespace);

        return ideConfigs.list().getItems();
    }

    public List<IdeConfig> getIdeConfigs(String namespace) {
        List<IdeConfig> configs = fetchIdeConfigs(namespace);

        List<IdeConfig> configList = new ArrayList<>();
        if (configs.isEmpty()) {
            log.info("No IdeConfigs found in namespace: " + namespace);
        } else {
            for (IdeConfig config : configs) {
                if (config != null) {
                    configList.add(config);
                    log.info("Found IdeConfig: " + config.getMetadata());
                }
            }
        }

        return configList;
    }

    public List<IdeConfig> getIdeConfig(String namespace, String ideConfigName) {
        List<IdeConfig> configs = fetchIdeConfigs(namespace);
        List<IdeConfig> matchingConfig = new ArrayList<>();

        if (configs.isEmpty()) {
            log.info("No IdeConfigs found in namespace: " + namespace);
        } else {
            for (IdeConfig config : configs) {
                if (config != null && ideConfigName.equals(config.getSpec().getUserName())) {
                    matchingConfig.add(config);
                    log.info("Found matching IdeConfig: " + config.getMetadata());
                }
            }
        }

        return matchingConfig;
    }


    // updateIdeConfig 메소드 추가
    public Optional<IdeConfig> updateIdeConfig(String namespace, String ideConfigName, IdeConfigSpec ideConfigSpec) {
        log.info("Updating IdeConfig: " + ideConfigName + " in namespace: " + namespace);
        NonNamespaceOperation<IdeConfig, IdeConfigList, Resource<IdeConfig>> ideConfigs =
                client.resources(IdeConfig.class, IdeConfigList.class).inNamespace(namespace);

        Resource<IdeConfig> ideConfigResource = ideConfigs.withName(ideConfigName);
        IdeConfig existingIdeConfig = ideConfigResource.get();
        if (existingIdeConfig == null) {
            log.warn("IdeConfig not found: " + ideConfigName + " in namespace: " + namespace);
            return Optional.empty();
        }

        // 업데이트할 IdeConfigSpec의 null이 아닌 필드만 적용
        IdeConfigSpec configSpec = applyNonNullFields(existingIdeConfig.getSpec(), ideConfigSpec);

        // Kubernetes 클라이언트를 사용하여 업데이트
        ideConfigResource.edit(c -> {
            c.setSpec(configSpec);
            return c;
        });

        log.info("IdeConfig updated successfully: " + ideConfigName);
        return Optional.ofNullable(ideConfigResource.get());
    }

    // null이 아닌 필드만 적용하는 메소드
    private IdeConfigSpec applyNonNullFields(IdeConfigSpec existingSpec, IdeConfigSpec newSpec) {
        if (newSpec.getUserName() != null) {
            existingSpec.setUserName(newSpec.getUserName());
        }
        if (newSpec.getServiceTypes() != null && !newSpec.getServiceTypes().isEmpty()) {
            existingSpec.setServiceTypes(newSpec.getServiceTypes());
        }

        if(newSpec.getVscode() != null) {
            existingSpec.setVscode(newSpec.getVscode());
        }
        if(newSpec.getWebssh() != null) {
            existingSpec.setWebssh(newSpec.getWebssh());
        }
        if(newSpec.getPortList() != null && !newSpec.getPortList().isEmpty()) {
            existingSpec.setPortList(newSpec.getPortList());
        }
        if(newSpec.getInfrastructureSize() != null) {
            existingSpec.setInfrastructureSize(newSpec.getInfrastructureSize());
        }

        if(newSpec.getReplicas() > 0 ) {
            existingSpec.setReplicas(newSpec.getReplicas());
        }

        return existingSpec;
    }



    /*

        public List<IdeConfigSpec> getIdeConfigs(String namespace) {
        List<IdeConfig> configs = fetchIdeConfigs(namespace);

        List<IdeConfigSpec> specList = new ArrayList<>();
        if (configs.isEmpty()) {
            log.info("No IdeConfigs found in namespace: " + namespace);
        } else {
            for (IdeConfig config : configs) {
                IdeConfigSpec spec = config.getSpec();
                if (spec != null) {
                    specList.add(spec);
                    log.info("Found IdeConfig: " + config.getMetadata());
                }
            }
        }

        return specList;
    }

    public List<IdeConfigSpec> getIdeConfig(String namespace, String ideConfigName) {
        List<IdeConfig> configs = fetchIdeConfigs(namespace);

        List<IdeConfigSpec> matchingSpecs = new ArrayList<>();
        if (configs.isEmpty()) {
            log.info("No IdeConfigs found in namespace: " + namespace);
        } else {
            for (IdeConfig config : configs) {
                IdeConfigSpec spec = config.getSpec();
                if (spec != null && ideConfigName.equals(spec.getUserName())) {
                    matchingSpecs.add(spec);
                    log.info("Found matching IdeConfig: " + config.getMetadata());
                }
            }
        }

        return matchingSpecs;
    }

     */

}
