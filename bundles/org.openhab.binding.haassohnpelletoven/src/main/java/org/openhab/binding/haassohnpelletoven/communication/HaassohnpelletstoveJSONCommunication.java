/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.haassohnpelletoven.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.haassohnpelletoven.data.HaassohnpelletstoveJsonData;
import org.openhab.binding.haassohnpelletoven.encryption.MD5Utils;
import org.openhab.binding.haassohnpelletoven.helper.Helper;
import org.openhab.binding.haassohnpelletoven.internal.HaassohnpelletstoveConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class handles the JSON communication with the Wifi Modul of the Stove
 *
 * @author Christian Feininger - Initial contribution
 *
 */
public class HaassohnpelletstoveJSONCommunication {

    private final Logger logger = LoggerFactory.getLogger(HaassohnpelletstoveJSONCommunication.class);
    private @Nullable HaassohnpelletstoveConfiguration config;

    private Gson gson;
    private String x_hs_pin;
    private HaassohnpelletstoveJsonData ovenData;

    public HaassohnpelletstoveJSONCommunication() {
        gson = new Gson();
        ovenData = new HaassohnpelletstoveJsonData();
    }

    /**
     * Refreshes the oven Connection with the internal oven token.
     *
     * @param message Message object to pass errors to the calling method.
     * @param thingUID Thing UID for logging purposes
     * @return true if no error occurred, false otherwise.
     */
    @SuppressWarnings("null")
    public boolean refreshOvenConnection(Helper message, String thingUID) {
        if (config == null) {
            message.setStatusDescription("Error in configuration. Please recreate Thing.");
            return false;
        }

        HaassohnpelletstoveJsonData result = null;
        boolean resultOk = false;
        String error = "", errorDetail = "", statusDescr = "";
        if (config == null) {
            return false;
        }
        @SuppressWarnings("null")
        String urlStr = "http://" + config.hostIP + "/status.cgi";

        String response = null;
        try {
            response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("OvenData = {}", response);

            result = gson.fromJson(response, HaassohnpelletstoveJsonData.class);
            resultOk = true;

        } catch (IllegalArgumentException e) {
            logger.debug("Illegal argument {}", e.getMessage());

            error = "Error creating URI with Host IP parameter: '" + config.hostIP + "'";
            errorDetail = e.getMessage();
            statusDescr = "Error contacting the oven with the IP" + config.hostIP;
            resultOk = false;
        } catch (IOException e) {
            logger.debug("Error processiong Get request {}", urlStr);
            statusDescr = "Timeout error with" + config.hostIP
                    + ". Cannot find service on give IP. Please verify the IP-Address!";
            errorDetail = e.getMessage();
            resultOk = false;
        } catch (Exception e) {
            logger.debug("Unknwon Error: {}", e.getMessage());
            errorDetail = e.getMessage();
            resultOk = false;
        }

        if (resultOk) {

            ovenData = result;
            x_hs_pin = getValidXHSPIN(ovenData);
        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", thingUID, error, errorDetail);
            ovenData = null;
        }
        message.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Gets the status of the oven
     *
     * @return true if success or false in case of error
     */
    @SuppressWarnings("null")
    public boolean updateOvenData(@Nullable String postData, Helper helper, String thingUID) {

        String statusDescr = "";
        boolean resultOk = false;
        String error = "", errorDetail = "";
        if (config == null) {
            return false;
        }
        @SuppressWarnings("null")
        String urlStr = "http://" + config.hostIP + "/status.cgi";

        // Run the HTTP POST request and get the JSON response from Oven
        String response = null;

        Properties httpHeader = new Properties();

        if (postData != null) {

            InputStream targetStream = new ByteArrayInputStream(postData.getBytes());
            try {

                refreshOvenConnection(helper, thingUID);
                httpHeader = createHeader(postData);
                response = HttpUtil.executeUrl("POST", urlStr, httpHeader, targetStream, "application/json", 10000);
                resultOk = true;
                logger.debug("Execute POST request with content to {} with header: {}", urlStr, httpHeader.toString());
            } catch (IOException e) {
                logger.debug("Error processiong POST request {}", urlStr);
                statusDescr = "Cannot execute command on Stove. Please verify connection and Thing Status";
                resultOk = false;
            }
        } else {

            try {
                refreshOvenConnection(helper, thingUID);
                httpHeader = createHeader(null);
                response = HttpUtil.executeUrl("POST", urlStr, httpHeader, null, "", 10000);
                resultOk = true;
                logger.debug("Execute POST request to {} with header: {}", urlStr, httpHeader.toString());
            } catch (IOException e) {
                logger.debug("Error processiong POST request {}", e.getMessage());
                if (e.getMessage().contains("Authentication challenge without WWW-Authenticate ")) {
                    statusDescr = "Cannot connect to stove. Given PIN: " + config.hostPIN + " is incorrect!";
                } else {
                    statusDescr = "Cannot connect to Stove. Please verify conection " + config.hostIP + " and PIN:"
                            + config.hostPIN;
                }
                resultOk = false;
            }

        }
        if (resultOk) {
            logger.debug("OvenData = {}", response);

            ovenData = gson.fromJson(response, HaassohnpelletstoveJsonData.class);

        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", thingUID, error, errorDetail);
            ovenData = null;

        }
        helper.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Creates the header for the Post Request
     *
     * @return The created Header Properties
     */
    @SuppressWarnings("null")
    private Properties createHeader(@Nullable String postData) {
        Properties httpHeader = new Properties();
        httpHeader.setProperty("Host", config.hostIP);
        httpHeader.setProperty("Accept", "*/*");
        httpHeader.setProperty("Proxy-Connection", "keep-alive");
        httpHeader.setProperty("X-BACKEND-IP", "https://app.haassohn.com");
        httpHeader.setProperty("Accept-Language", "de-DE;q=1.0, en-DE;q=0.9");
        httpHeader.setProperty("Accept-Encoding", "gzip;q=1.0, compress;q=0.5");
        httpHeader.setProperty("token", "32 bytes");
        httpHeader.setProperty("Content-Type", "application/json");
        if (postData != null) {
            int a = postData.getBytes().length;
            httpHeader.setProperty(x_hs_pin, Integer.toString(a));
        }
        httpHeader.setProperty("User-Agent", "ios");
        httpHeader.setProperty("Connection", "keep-alive");
        httpHeader.setProperty("X-HS-PIN", x_hs_pin);
        return httpHeader;
    }

    /**
     * Generate the valid encrypted string to communicate with the oven.
     *
     * @param ovenData
     * @return
     */
    private @Nullable String getValidXHSPIN(HaassohnpelletstoveJsonData ovenData) {

        if (ovenData != null) {

            String nonce = ovenData.getNonce();

            @SuppressWarnings("null")
            String ePin = MD5Utils.getMD5String(config.hostPIN);
            return MD5Utils.getMD5String(nonce + ePin);
        } else {
            return null;
        }
    }

    /**
     * Set the config for service to communicate
     *
     * @param config2
     */
    public void setConfig(@Nullable HaassohnpelletstoveConfiguration config2) {
        this.config = config2;
    }

    /**
     * Returns the actual stored Oven Data
     *
     * @return
     */
    public HaassohnpelletstoveJsonData getOvenData() {
        return this.ovenData;
    }
}
