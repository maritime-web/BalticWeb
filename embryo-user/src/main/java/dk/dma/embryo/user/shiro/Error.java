/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.user.shiro;


/**
 * @author Jesper Tejlgaard
 */
public class Error {
    
    private AuthCode authCode;
    private String message;
    
    public Error(AuthCode authCode, String message) {
        super();
        this.authCode = authCode;
        this.message = message;
    }

    public AuthCode getAuthCode() {
        return authCode;
    }

    public void setAuthCode(AuthCode authCode) {
        this.authCode = authCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static enum AuthCode{
        UNAUTHENTICATED, UNAUTHORIZED;
    }

}
