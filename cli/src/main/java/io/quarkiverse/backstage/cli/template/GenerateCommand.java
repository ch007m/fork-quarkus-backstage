package io.quarkiverse.backstage.cli.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.quarkiverse.backstage.cli.common.GenerationBaseCommand;
import io.quarkiverse.backstage.client.BackstageClient;
import io.quarkiverse.backstage.common.handlers.GetBackstageTemplatesHandler;
import io.quarkiverse.backstage.spi.DevTemplateBuildItem;
import io.quarkiverse.backstage.spi.TemplateBuildItem;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "generate", sortOptions = false, mixinStandardHelpOptions = false, header = "Generate Backstage Template.", headerHeading = "%n", commandListHeading = "%nCommands:%n", synopsisHeading = "%nUsage: ", optionListHeading = "%nOptions:%n")
public class GenerateCommand extends GenerationBaseCommand<List<TemplateBuildItem>> {

    @Option(names = { "--name" }, description = "The template name")
    Optional<String> name = Optional.empty();

    @Option(names = { "--dev-template" }, description = "Flag for also generating a dev template. Default is false.")
    boolean generateDevTemplate;

    @Option(names = {
            "--health-endpoint" }, description = "Flag for exposing health endpoint in the template. Default is false.")
    boolean exposeHealthEndpoint;

    @Option(names = {
            "--metrics-endpoint" }, description = "Flag for exposing metrics endpoint in the template. Default is false.")
    boolean metricsEndpoint;

    @Option(names = { "--info-endpoint" }, description = "Flag for exposing info endpoint in the template. Default is false.")
    boolean infoEndpoint;

    public GenerateCommand(BackstageClient backstageClient) {
        super(backstageClient);
    }

    @Override
    public String getHandlerName() {
        return GetBackstageTemplatesHandler.class.getName();
    }

    @Override
    public String[] getRequiredBuildItems() {
        if (generateDevTemplate) {
            return new String[] { TemplateBuildItem.class.getName(), DevTemplateBuildItem.class.getName() };
        }
        return new String[] { TemplateBuildItem.class.getName() };
    }

    @Override
    public Properties getBuildSystemProperties() {
        Properties properties = super.getBuildSystemProperties();
        if (generateDevTemplate) {
            properties.put("quarkus.backstage.dev-template.generation.enabled", "true");
        }
        if (exposeHealthEndpoint) {
            properties.put("quarkus.backstage.template.parameters.endpoints.health.enabled", "true");
        }
        if (metricsEndpoint) {
            properties.put("quarkus.backstage.template.parameters.endpoints.metrics.enabled", "true");
        }
        if (infoEndpoint) {
            properties.put("quarkus.backstage.template.parameters.endpoints.info.enabled", "true");
        }
        return properties;
    }

    @Override
    public void process(List<TemplateBuildItem> templateBuildItems) {
        List<TemplateListItem> items = new ArrayList<>();
        for (TemplateBuildItem templateBuildItem : templateBuildItems) {
            String templateName = templateBuildItem.getTemplate().getMetadata().getName();
            Map<Path, String> templateContent = templateBuildItem.getContent();
            templateContent.forEach((path, content) -> {
                try {
                    Files.createDirectories(path.getParent());
                    Files.writeString(path, content);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write file: " + path, e);
                }
            });
            items.add(TemplateListItem.from(templateBuildItem.getTemplate()));
        }

        TemplateListTable table = new TemplateListTable(items);
        System.out.println(table.getContent());
    }
}
