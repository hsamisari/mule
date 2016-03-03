/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime.connector.secure;

import org.mule.extension.api.annotation.Extension;
import org.mule.extension.api.annotation.Operations;
import org.mule.extension.api.annotation.Parameter;
import org.mule.extension.api.annotation.capability.Xml;
import org.mule.extension.api.annotation.connector.Providers;
import org.mule.extension.api.annotation.param.display.Password;
import org.mule.extension.api.annotation.param.display.Text;

@Extension(name = "secure", description = "Secure Test connector")
@Operations(SecureOperations.class)
@Providers(SecureConnectionProvider.class)
@Xml(schemaLocation = "http://www.mulesoft.org/schema/mule/secure", namespace = "secure", schemaVersion = "4.0")
public class SecureConnector
{

    @Parameter
    @Text
    private String plainStringField;

    @Parameter
    @Password
    private String password;


}
