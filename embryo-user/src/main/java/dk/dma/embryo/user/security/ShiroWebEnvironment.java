/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.user.security;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.user.service.JpaRealm;

public class ShiroWebEnvironment extends IniWebEnvironment {

    // No CDI support. Constructor also before CDI has been enabled. Thus creating logger manually. 
    private final Logger logger = LoggerFactory.getLogger(ShiroWebEnvironment.class);
    
    public ShiroWebEnvironment() {
        logger.info("Initialising Shiro security for Embryonic e-Navigation Application");

        Ini ini = new Ini();
        
        Section s = ini.addSection("main");
        s.put("credentialsMatcher", HashedCredentialsMatcher.class.getName());
        s.put("credentialsMatcher.hashAlgorithmName", "SHA-512");
        s.put("credentialsMatcher.hashIterations", "10000");
        s.put("credentialsMatcher.storedCredentialsHexEncoded", "false");
        s.put("realm", JpaRealm.class.getName());
        s.put("realm.credentialsMatcher", "$credentialsMatcher");
        setIni(ini);
    }

}
