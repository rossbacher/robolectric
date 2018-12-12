package org.robolectric.internal.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.robolectric.res.FsFile;
import org.robolectric.util.Util;

public class PropertiesDependencyResolver implements DependencyResolver {
  private final Properties properties;
  private final FsFile baseDir;
  private DependencyResolver delegate;

  public PropertiesDependencyResolver(FsFile propertiesFile, DependencyResolver delegate) throws IOException {
    this.properties = loadProperties(propertiesFile);
    this.baseDir = propertiesFile.getParent();
    this.delegate = delegate;
  }

  private Properties loadProperties(FsFile propertiesFile) throws IOException {
    final Properties properties = new Properties();
    try (InputStream stream = propertiesFile.getInputStream()) {
      properties.load(stream);
    }
    return properties;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    String depShortName = dependency.getShortName();
    String path = properties.getProperty(depShortName);
    if (path != null) {
      if (path.indexOf(File.pathSeparatorChar) != -1) {
        throw new IllegalArgumentException("didn't expect multiple files for " + dependency
            + ": " + path);
      }

      if (!new File(path).isAbsolute()) {
        path = new File(baseDir.getPath(), path).getPath();
      }
      try {
        return Util.url(path);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    } else {
      if (delegate != null) {
        return delegate.getLocalArtifactUrl(dependency);
      }
    }

    throw new RuntimeException("no artifacts found for " + dependency);
  }
}
