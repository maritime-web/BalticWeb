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

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import dk.dma.embryo.user.model.SecuredUser;

/**
 * Utility class with factory method. 
 * 
 * 
 * @author Jesper Tejlgaard
 * 
 */
public class SecurityUtil{

    
    public static SecuredUser createUser(String userName, String password, String email){
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        ByteSource salt = rng.nextBytes();

//        HashedCredentialsMatcher matcher = (HashedCredentialsMatcher)jpaRealm.getCredentialsMatcher();
        String algorithmName = "SHA-512";//matcher.getHashAlgorithmName();
        int iterations = 10000;//matcher.getHashIterations();

        //Now hash the plain-text password with the random salt and multiple
        //iterations and then Base64-encode the value (requires less space than Hex):
        String hashedPasswordBase64 = new SimpleHash(algorithmName, password, salt, iterations).toBase64();
        
        SecuredUser user = new SecuredUser(userName, hashedPasswordBase64, salt.getBytes(), email);
        
        return user;
    }
    
}
