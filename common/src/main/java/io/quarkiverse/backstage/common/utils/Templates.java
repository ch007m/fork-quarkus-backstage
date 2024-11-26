package io.quarkiverse.backstage.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import io.quarkiverse.backstage.common.dsl.GitActions;
import io.quarkiverse.backstage.scaffolder.v1beta3.Template;
import io.quarkiverse.backstage.spi.TemplateBuildItem;

public final class Templates {

    private static final Pattern YAML_URL_PATTERN = Pattern.compile("^(https?://)?.*\\.ya?ml$", Pattern.CASE_INSENSITIVE);
    private static final Comparator<Path> PATH_COMPARATOR = (left, right) -> left.getNameCount() - right.getNameCount();

    private Templates() {
        //Utility class
    }

    public static Path getTemplatePath(Map<Path, String> content) {
        return content.keySet().stream().filter(path -> path.endsWith("template.yaml")).sorted(PATH_COMPARATOR).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No template.yaml found"));
    }

    public static Template getTemplate(Map<Path, String> content) {
        Path templatePath = getTemplatePath(content);
        String templateContent = content.get(templatePath);
        Template template = Serialization.unmarshal(templateContent, Template.class);
        return template;
    }

    public static TemplateBuildItem downloadTemplate(String url) {
        if (!YAML_URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        if (Github.isGithubUrl(url)) {
            System.out.println("Cloning template from GitHub: " + url);
            String cloneUrl = Github.toSshCloneUrl(url);
            Path cloneDir = GitActions.cloneToTemp(cloneUrl).getRepositoryRootPath();
            Path relativeTemplateYamlPath = Github.toRelativePath(url);
            Path templatePath = cloneDir.resolve(relativeTemplateYamlPath);
            Path templateDir = templatePath.getParent();
            return createTemplateBuildItem(templateDir);
        }
        throw new IllegalArgumentException("Unsupported URL: " + url);
    }

    public static TemplateBuildItem createTemplateBuildItem(Path sourceTemplateDir) {
        Map<Path, String> templateContent = new HashMap<>();
        Path templatePath = sourceTemplateDir.resolve("template.yaml");
        Template template = Serialization.unmarshal(templatePath.toFile(), Template.class);
        templateContent.put(templatePath, Serialization.asYaml(template));

        try {
            Files.walk(sourceTemplateDir).forEach(p -> {
                if (!p.toFile().isDirectory()) {
                    templateContent.put(p, Strings.read(p));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new TemplateBuildItem(template, templateContent);
    }

    public static TemplateBuildItem move(TemplateBuildItem templateBuildItem, Path targetTemplateDir) {
        Path templatePath = getTemplatePath(templateBuildItem.getContent());
        Optional<Path> templateDirPath = Optional.ofNullable(templatePath.getParent());
        Optional<Path> sourceTemplateDir = templateDirPath.map(Path::getParent);
        return move(templateBuildItem, sourceTemplateDir, targetTemplateDir);
    }

    public static TemplateBuildItem move(TemplateBuildItem templateBuildItem, Optional<Path> sourceTemplateDir,
            Path targetTemplateDir) {
        Map<Path, String> templateContent = new HashMap<>();
        templateBuildItem.getContent().forEach((path, content) -> {
            sourceTemplateDir.ifPresentOrElse(dir -> {
                templateContent.put(targetTemplateDir.resolve(dir.relativize(path)), content);
            }, () -> {
                templateContent.put(targetTemplateDir.resolve(path), content);
            });
        });

        return new TemplateBuildItem(templateBuildItem.getTemplate(), templateContent);
    }
}
