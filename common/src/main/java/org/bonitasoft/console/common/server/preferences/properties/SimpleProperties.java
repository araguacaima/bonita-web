/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.common.server.preferences.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;

/**
 * @author Ruiheng Fan, Anthony Birembaut
 */
public class SimpleProperties {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(SimpleProperties.class.getName());

    /**
     * The loaded properties
     */
    protected Properties properties = new Properties();

    /**
     * The properties file
     */
    protected File propertiesFile;

    /**
     * Private contructor to prevent instantiation
     *
     * @throws IOException
     */
    public SimpleProperties(final File propertiesFile) {
        this.propertiesFile = propertiesFile;
        InputStream inputStream = null;
        try {
            if (!propertiesFile.exists()) {
                initProperties(propertiesFile);
            }
            inputStream = new FileInputStream(propertiesFile);
            properties.load(inputStream);
        } catch (final IOException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Bonita web preferences file " + propertiesFile.getPath() + " could not be loaded.", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "Bonita web preferences file stream " + propertiesFile.getPath() + " could not be closed.", e);
                    }
                }
            }
        }
    }

    public SimpleProperties(final Properties properties) {
        this.properties = properties;
    }

    protected static File getTenantPropertiesFile(final long tenantId, final String propertiesFileName) {
        return new File(WebBonitaConstantsUtils.getInstance(tenantId).getConfFolder(), propertiesFileName);
    }

    protected void initProperties(final File aPropertiesFile) throws IOException {
        // Create the file.
        aPropertiesFile.createNewFile();
    }

    public String getProperty(final String propertyName) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(propertyName);
    }

    public Set<String> getPropertiesNames() {
        if (properties == null) {
            return Collections.emptySet();
        }
        return properties.stringPropertyNames();
    }

    public void removeProperty(final String propertyName) throws IOException {
        if (properties != null) {
            properties.remove(propertyName);
            if (propertiesFile != null) {
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(propertiesFile);
                    properties.store(outputStream, null);
                } catch (final IOException e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "Bonita web preferences file " + propertiesFile.getPath() + " could not be loaded.", e);
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (final IOException e) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING, "Bonita web preferences file stream " + propertiesFile.getPath() + " could not be closed.", e);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setProperty(final String propertyName, final String propertyValue) throws IOException {
        if (properties != null) {
            properties.setProperty(propertyName, propertyValue);
            if (propertiesFile != null) {
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(propertiesFile);
                    properties.store(outputStream, null);
                } catch (final IOException e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "Bonita web preferences file " + propertiesFile.getPath() + " could not be loaded.", e);
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (final IOException e) {
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING, "Bonita web preferences file stream " + propertiesFile.getPath() + " could not be closed.", e);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<String> getPropertyAsList(final String propertyName) {
        final String propertyAsString = getProperty(propertyName);
        return stringToList(propertyAsString.trim());
    }

    protected List<String> stringToList(final String propertyValueAsString) {
        if (propertyValueAsString != null) {
            final List<String> propertiesList = new ArrayList<String>();
            if (propertyValueAsString.startsWith("[") && propertyValueAsString.endsWith("]")) {
                String propertyCSV = propertyValueAsString.substring(1, propertyValueAsString.length() - 1);
                propertyCSV = propertyCSV.trim();
                if(propertyCSV.isEmpty()){
                    return Collections.emptyList();
                }
                final String[] propertyArray = propertyCSV.split(",");
                for (final String propertyValue : propertyArray) {
                    propertiesList.add(propertyValue.trim());
                }
            } else {
                propertiesList.add(propertyValueAsString);
            }
            return propertiesList;
        } else {
            return Collections.emptyList();
        }
    }

    public void setPropertyAsList(final String property, final Set<String> permissions) throws IOException {
        setProperty(property, permissions.toString());
    }
}