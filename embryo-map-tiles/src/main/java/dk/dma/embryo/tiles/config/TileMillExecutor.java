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

package dk.dma.embryo.tiles.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class TileMillExecutor {

    File executionDirectory;
    File tileMillDirectory;

    public TileMillExecutor(File executionDirectory, File tileMillDirectory) {
        if (!executionDirectory.exists()) {
            throw new IllegalArgumentException("Unknown directory");
        }

        File indexJs = new File(executionDirectory, "index.js");
        if (!indexJs.exists()) {
            throw new IllegalArgumentException("projectmill index.js cound not be found in " + executionDirectory.getAbsolutePath());
        }
        this.executionDirectory = executionDirectory;

        this.tileMillDirectory = tileMillDirectory;
    }

    public TileMillExecutor(String executionDirectory, String tileMillDirectory) {
        this(new File(executionDirectory), new File(tileMillDirectory));
    }

    public void execute(File configFile) throws IOException {

        String config = "-c " + configFile.getAbsolutePath();
        //String config = "-c embryo-satellite-images/target/test-classes/projectmill.testconfig.json";
        //String tileMill = " -t " + tileMillDirectory.getAbsolutePath();

        System.out.println(config);
        System.out.println(executionDirectory);

        ProcessBuilder builder = new ProcessBuilder("/home/jesper/Git/projectmill/index.js", "--mill", "--render", "-c", "/home/jesper/Git/Embryo/embryo-satellite-images/target/test-classes/projectmill.testconfig.json");
        builder.redirectError(new File("-error.log"));
        builder.redirectOutput(new File("-output.log"));


        try {
            String cmd = "/home/jesper/Git/projectmill/index.js --mill --render " + config;

            System.out.println(cmd);
            Process proc = builder.start();
            //Process proc=Runtime.getRuntime().exec(cmd, null, executionDirectory);
            proc.waitFor();

            //System.out.println(builder.command());
            //System.out.println(builder.directory());

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));


            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            System.out.println("Here is the standard error of the command:\n");
            s = null;
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
