/*
 * Copyright (c) 2011 Danish Maritime Authority.
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
var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;

var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = function (grunt) {
    // Project configuration.
    grunt.initConfig({
        proj: {
            name: grunt.file.readJSON('package.json').name,
            src: 'src/main/webapp',
            test: 'src/test/jsUnit',
            build: 'target/build',
            livereload: '.tmp/livereload',
            target: 'target',
            dist: 'target/webapp'
        },
        watch: {
            webapp: {
                files: [ '<%= proj.src %>/{,**/}*.*' ],
                tasks: [ 'copy:all2Livereload' ]
            },
            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [ '<%= proj.livereload %>/{,*/}*.html', '<%= proj.livereload %>/css/{,*/}*.css',
                    '<%= proj.livereload %>/js/{,*/}*.js',
                    '<%= proj.livereload %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}' ]
            }
        },

        connect: {
            options: {
                port: 9000,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname: '0.0.0.0',
                livereload: 35729
            },
            proxies: [
                {
                    context: [ '/rest' ],
                    host: 'localhost',
                    port: '8080',
                    https: false,
                    changeOrigin: true,
                    xforward: false
                }
            ],
            livereload: {
                options: {
                    open: true,
                    base: [ '<%= proj.livereload %>' ],
                    middleware: function (connect, options) {
                        return [ proxySnippet, mountFolder(connect, '.tmp/livereload') ];
                        // return [ connect.static('<%= proj.src %>'),
                        // proxySnippet ];
                    }
                }
            },
            // test : {
            // options : {
            // port : 9001,
            // base : [ '.tmp', 'test', '<%= yeoman.app %>' ]
            // }
            // },
            // dist : {
            // options : {
            // base : '<%= yeoman.dist %>'
            // }
            // }
        },

        // concat : {
        // options : {
        // // define a string to put between each file in the concatenated
        // // output
        // separator : ';'
        // },
        // dist : {
        // // the files to concatenate
        // src : [ '.tmp/**/arcticweb.js' ],
        // // the location of the resulting JS file
        // dest : '<%= proj.build %>/js/<%= proj.name %>.js'
        // }
        // },
        // uglify : {
        // options : {
        // banner : '! <%= proj.name %> <%= grunt.template.today("yyyy-mm-dd")
        // %> \n'
        // },
        // build : {
        // src : '<%= proj.build %>/js/<%= proj.name %>.js',
        // dest : '<%= proj.build %>/js/<%= proj.name %>.min.js'
        // }
        // },
        useminPrepare: {
            html: [ '<%= proj.src %>/*.html'],
            options: {
                dest: '<%= proj.build %>',
                staging: '<%= proj.target %>'
                // flow : {
                // html : {
                // steps : {
                // 'js' : [ 'concat' ]
                // },
                // post : {}
                // }
                // }
            }
        },
        usemin: {
            html: [ '<%= proj.build %>/{,*/}*.html' ],
            options: {
                dirs: [ '<%= proj.build %>' ]
            }
        },
        // Put files not handled in other tasks here
        copy: {
            toTarget: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= proj.build %>',
                        src: '{,*/}*',
                        dest: '<%= proj.dist %>'
                    }
                ]
            },
            unMod2Build: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= proj.src %>',
                        src: ['{,*/}*.html', 'css/ext/{,*/}*.css'],
                        dest: '<%= proj.build %>'
                    }
                ]
            },
            gen2Build: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= proj.target %>/concat',
                        src: '{,*/}*.js',
                        dest: '<%= proj.build %>'
                    },
                    {
                        expand: true,
                        cwd: '<%= proj.target %>/concat',
                        src: '{,*/}*.css',
                        dest: '<%= proj.build %>'
                    }
                ]
            },
            all2Livereload: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= proj.src %>',
                        src: '{,**/}*.*',
                        dest: '<%= proj.livereload %>/'
                    }
                ]
            }
        },
        clean: {
            // dist: {
            // files: [{
            // dot: true,
            // src: [
            // '.tmp',
            // '<%= yeoman.dist %>/*',
            // '!<%= yeoman.dist %>/.git*'
            // ]
            // }]
            // },
            server: '<%= proj.build %>'
        },
        replace: {
            run: {
                src: ['<%= proj.build %>/*.{html,appcache}', '<%= proj.build %>/docs/index.html'],
                overwrite: true,                 // overwrite matched source files
                replacements: [
                    {
                        from: '/js/cached/common/cdn.cloudflare', // string replacement
                        to: '//cdnjs.cloudflare.com/ajax/libs'
                    },
                    {
                        from: 'js/cached/common/cdn.cloudflare', // string replacement
                        to: '//cdnjs.cloudflare.com/ajax/libs'
                    },
                    {
                        from: '/js/cached/common/cdn.googleapis', // string replacement
                        to: '//ajax.googleapis.com/ajax/libs'
                    },
                    {
                        from: 'js/cached/common/cdn.googleapis', // string replacement
                        to: '//ajax.googleapis.com/ajax/libs'
                    },
                    {
                        from: '/js/cached/front/cdn.googleapis', // string replacement
                        to: '//ajax.googleapis.com/ajax/libs'
                    },
                    {
                        from: 'js/cached/front/cdn.googleapis', // string replacement
                        to: '//ajax.googleapis.com/ajax/libs'
                    },
                    {
                        from: '/js/cached/map/cdn.cloudflare', // string replacement
                        to: '//cdnjs.cloudflare.com/ajax/libs'
                    },
                    {
                        from: 'js/cached/map/cdn.cloudflare', // string replacement
                        to: '//cdnjs.cloudflare.com/ajax/libs'
                    },
                    {
                        from: '/js/cached/map/cdn.netdna', // string replacement
                        to: '//netdna.bootstrapcdn.com'
                    },
                    {
                        from: 'js/cached/map/cdn.netdna', // string replacement
                        to: '//netdna.bootstrapcdn.com'
                    },
                    {
                        from: '/css/cached/cdn.netdna', // string replacement
                        to: '//netdna.bootstrapcdn.com'
                    },
                    {
                        from: 'css/cached/cdn.netdna', // string replacement
                        to: '//netdna.bootstrapcdn.com'
                    },
                    {
                        from: '/js/cached/map/cdn.firebase', // string replacement
                        to: '//cdn.firebase.com/'
                    },
                    {
                        from: 'js/cached/map/cdn.firebase', // string replacement
                        to: '//cdn.firebase.com/'
                    },
                ]
            }
        },
        cdnify: {
            cdn: {
                options: {
                    rewriter: function (url) {
                        url = url.replace("js/ext/cdn.cloudflare", "//cdnjs.cloudflare.com/ajax/libs");
                        url = url.replace("js/ext/cdn.googleapis", "//ajax.googleapis.com/ajax/libs");
                        url = url.replace("js/ext/cdn.netdna", "//netdna.bootstrapcdn.com");
                        url = url.replace("css/ext/cdn.netdna", "//netdna.bootstrapcdn.com");
                        return url;
                    }
                },
                files: [
                    {
                        expand: true,
                        cwd: '<%= proj.build %>/webapp',
                        src: '*.{html}',
                        dest: '<%= proj.build %>'
                    }
                ]
            }
        },
        appcache: {
            options: {
                basePath: 'src/main/webapp'
            },
            map: {
                dest: 'target/build/map.appcache',
                cache: {
                    literals: [//as is in the "CACHE:" section
                        'map.html',
                        'css/arcticweb-map.css',
                        'css/arcticweb-map-ext-lib.css',
                        'js/arcticweb-map.js',
                        'css/cached/cdn.netdna/font-awesome/4.3.0/fonts/fontawesome-webfont.woff2?v=4.3.0',
                        'rest/shapefile/static/multiple/static.world_merc?delta=true&exponent=2'
                    ],
                    patterns: [
                        'src/main/webapp/css/cached/**',
                        'src/main/webapp/js/cached/common/**',
                        'src/main/webapp/js/cached/map/**',
                        'src/main/webapp/partials/common/*.html',
                        'src/main/webapp/partials/*.html',
                        'src/main/webapp/img/**/*.png',
                        'src/main/webapp/img/**/*.jpg',
                        'src/main/webapp/img/**/*.gif',
                        '!src/main/webapp/img/front/**/*', // except the 'img/front/' subtree
                        '!src/main/webapp/img/ext/**/*', // except the 'img/front/' subtree
                        '!src/main/webapp/img/unused/**/*', // except the 'img/front/' subtree
                        '!**/README.MD'
                    ]
                },
                network: "*"
            },
            front: {
                dest: 'target/build/front.appcache',
                cache: {
                    literals: [//as is in the "CACHE:" section
                        'index.html',
                        'content.html',
                        'css/arcticweb-front.css',
                        'css/arcticweb-content.css',
                        'js/arcticweb-front.js',
                        'js/arcticweb-content.js',
                        'css/cached/cdn.netdna/font-awesome/4.3.0/fonts/fontawesome-webfont.woff2?v=4.3.0',
                        'rest/shapefile/static/multiple/static.world_merc?delta=true&exponent=2'

                    ],
                    patterns: [
                        'src/main/webapp/css/cached/**',
                        'src/main/webapp/js/cached/common/**',
                        'src/main/webapp/js/cached/front/**',
                        'src/main/webapp/partials/common/*',
                        'src/main/webapp/partials/front/*',
                        'src/main/webapp/img/front/*.png',
                        'src/main/webapp/img/front/*.jpg',
                        'src/main/webapp/img/front/*.gif',
                        'src/main/webapp/img/common/*',
                        '!**/README.MD'
                    ]
                },
                network: "*"
            },
            usermanual: {
                dest: 'target/build/usermanual.appcache',
                cache: {
                    literals: [//as is in the "CACHE:" section
                        'css/cached/cdn.netdna/font-awesome/4.3.0/fonts/fontawesome-webfont.woff2?v=4.3.0',
                        'docs/index.html',
                        'docs/doc.css'
                    ],
                    patterns: [
                       	'src/main/webapp/docs/img/**',
                       	'src/main/webapp/css/cached/**',
                       	'src/main/webapp/js/cached/common/**',
                    	'src/main/webapp/js/cached/docs/**',
                       	
                    ]
                },
                network: "*"
            }
        },
        concurrent: {
            server: [ 'concat:dist' ],
            // test : [ 'coffee', 'copy:styles' ],
            build: [ 'copy:styles2Build', 'copy:html2Build' ]
        },
        karma: {
            options: {
                configFile: 'src/test/resources/karma.conf.js',
                browsers: [ 'Chrome', 'Firefox', 'PhantomJS' ]
            },
            continuous: {
                port: 5678,
                singleRun: true,
                browsers: [ 'PhantomJS' ]
            },
            unit: {
                singleRun: false,
                autoWatch: true,
                keepalive: true
            }

        }
    });

    // Load the plugin that provides the "uglify" task.
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-usemin');
    grunt.loadNpmTasks('grunt-concurrent');
    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-cdnify');
    grunt.loadNpmTasks('grunt-text-replace');
    grunt.loadNpmTasks('grunt-appcache');

    grunt.registerTask('server',
        function (target) {
            if (target === 'dist') {
                return grunt.task.run([ 'build', 'connect:dist:keepalive' ]);
            }

            grunt.task.run([ 'copy:all2Livereload', /* 'autoprefixer', */'configureProxies', 'connect:livereload',
                'watch' ]);
        });

    // grunt.registerTask('test', [ 'clean:server', 'concurrent:test',
    // 'autoprefixer', 'connect:test', 'karma' ]);

    grunt.registerTask('test', [ 'karma:continuous' ]);

    //grunt.registerTask('build', [ 'useminPrepare', 'copy:unMod2Build', 'concat', 'usemin',
    //   'copy:gen2Build', 'copy:toTarget', 'appcache:map', 'appcache:front']);

    grunt.registerTask('build', [ 'useminPrepare', 'copy:unMod2Build', 'replace:run', 'concat', 'usemin',
        'copy:gen2Build', 'appcache:map', 'appcache:front', 'appcache:usermanual', 'replace:run', 'copy:toTarget']);

    // 'clean:dist',
    // 'useminPrepare',
    // 'concurrent:dist',
    // 'autoprefixer',
    // 'concat',
    // 'copy:dist',
    // 'cdnify',
    // 'ngmin',
    // 'cssmin',
    // 'uglify',
    // 'rev',
    // 'usemin'

    // grunt.registerTask('build', [ 'clean:dist', 'useminPrepare',
    // 'concurrent:dist', 'autoprefixer', 'concat',
    // 'copy:dist', 'cdnify', 'ngmin', 'cssmin', 'uglify', 'rev', 'usemin' ]);

    grunt.registerTask('default', [ 'build' ]);

};