/**
 * Copyright ©2019 Accenture and/or its affiliates. All Rights Reserved.
 * <p>
 * Permission to any use, copy, modify, and distribute this software and
 * its documentation for any purpose is subject to a licensing agreement
 * duly entered into with the copyright owner or its affiliate.
 * <p>
 * All information contained herein is, and remains the property of Accenture
 * and/or its affiliates and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to Accenture and/or
 * its affiliates and its suppliers and may be covered by one or more patents
 * or pending patent applications in one or more jurisdictions worldwide,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Accenture and/or its affiliates.
 */
package org.ceskaexpedice.akubra;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Ancestor of integration tests
 *
 * @author ppodsednik
 */
public abstract class AbstractFunctionalTest {
  private static final String PROPERTIES = "functionalTests.properties";
  protected String skipProperty = "skipAkubraFunctionalTests";
  protected Properties properties;

  public void after() {
  }

  protected void setUp() {
    loadProperties();
    assumeTrue(!isIgnored(), "Test ignored by the property: " + skipProperty);
  }

  private void loadProperties() {
    if (properties == null) {
      properties = new Properties();
      try {
        properties.load(this.getClass().getClassLoader().getResourceAsStream(PROPERTIES));
      } catch (IOException e) {
        System.out.println("Cannot find property file, will continue anyway:" + PROPERTIES);
      }
    }
  }

  protected boolean isIgnored() {
    String ignore = getProperty(skipProperty, "true");
    return Boolean.valueOf(ignore);
  }

  protected String getProperty(String name, String defaultValue) {
    String val = getSysOrEnvProperty(name);
    if(isEmpty(val) && properties != null){
      val = (String) properties.get(name);
    }
    return isEmpty(val) ? defaultValue : val;
  }

  private static String getSysOrEnvProperty(String name) {
    String val = System.getProperty(name);
    return isEmpty(val) ? System.getenv(name) : val;
  }

  private static boolean isEmpty(String name) {
    if(name == null || name.isEmpty()){
      return true;
    }else{
      return false;
    }
  }

}
