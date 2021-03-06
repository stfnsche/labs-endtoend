package org.openengsb.labs.endtoend.distribution.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openengsb.labs.endtoend.distribution.ResolvedDistribution;
import org.openengsb.labs.endtoend.util.MavenConfigurationHelper;
import org.ops4j.pax.url.mvn.internal.AetherBasedResolver;

public class DistributionResolver {
    private static Pattern mvnVlessPattern = Pattern.compile("^mvn-vless:(.+):(.+):(.*):(.*)$");

    private final AetherBasedResolver aetherBasedResolver;

    public DistributionResolver() {
        try {
            this.aetherBasedResolver = new AetherBasedResolver(MavenConfigurationHelper.getUserConfig(new Properties()));
        } catch (Exception e) {
            throw new IllegalStateException("AetherBasedResolver could not be instantiated.", e);
        }
    }

    public ResolvedDistribution resolveDistribution(String distributionUrl) throws InvalidDistributionURI,
            DistributionResolveException {
        Matcher m = mvnVlessPattern.matcher(distributionUrl);
        if (!m.find()) {
            throw new InvalidDistributionURI(distributionUrl);
        }

        String groupId = m.group(1);
        String artifactId = m.group(2);
        String type = m.group(3);
        String classifier = m.group(4);

        String version = getArtifactVersion(m.group(1), m.group(2));

        File distributionFile;
        try {
            distributionFile = aetherBasedResolver.resolveFile(groupId, artifactId, classifier, type, version);
        } catch (IOException e) {
            throw new DistributionResolveException(e);
        }

        return new ResolvedDistribution(distributionFile);
    }

    private String getArtifactVersion(final String groupId, final String artifactId) {
        final Properties dependencies = new Properties();
        String version = null;

        try {
            dependencies.load(new FileInputStream(getFileFromClasspath("META-INF/maven/dependencies.properties")));
            version = dependencies.getProperty(groupId + "/" + artifactId + "/version");
            if (version == null) {
                throw new RuntimeException("Could not resolve version. Do you have a dependency for " + groupId + "/"
                        + artifactId + " in your maven project?");
            }
        } catch (FileNotFoundException e1) {
            try {
                version = getDefaultArtifactVersion(groupId, artifactId, dependencies, version);
            } catch (FileNotFoundException e2) {
                throw new RuntimeException(
                        "Neither depends-maven-plugin generated dependencies.properties file found, nor a default_dependencies.properties file.");
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "IO Error while reading depends-maven-plugin generated dependencies.properties file.");
        }

        return version;
    }

    private String getDefaultArtifactVersion(final String groupId, final String artifactId,
            final Properties dependencies, String version) throws FileNotFoundException {
        try {
            dependencies.load(new FileInputStream(getFileFromClasspath("default_dependencies.properties")));
            version = dependencies.getProperty(groupId + "/" + artifactId + "/version");
            if (version == null) {
                throw new RuntimeException("Could not resolve version. Do you have a dependency for " + groupId + "/"
                        + artifactId + " in your maven project?");
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("IO Error while reading default_dependencies.properties file.");
        }
        return version;
    }

    private File getFileFromClasspath(final String filePath) throws FileNotFoundException {
        try {
            URL fileURL = DistributionResolver.class.getClassLoader().getResource(filePath);
            if (fileURL == null) {
                throw new FileNotFoundException("File [" + filePath + "] could not be found in classpath");
            }
            return new File(fileURL.toURI());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException("File [" + filePath + "] could not be found: " + e.getMessage());
        }
    }

}
