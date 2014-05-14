var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;

var mountFolder = function(connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = function(grunt) {
    // Project configuration.
    grunt.initConfig({
        proj : {
            name : grunt.file.readJSON('package.json').name,
            src : 'src/main/webapp',
            test : 'src/test/jsUnit',
            build : '.tmp/webapp',
            livereload : '.tmp/livereload',
            dist : 'target/webapp'
        },
        watch : {
            webapp : {
                files : [ '<%= proj.src %>/{,**/}*.*' ],
                tasks : [ 'copy:all2Livereload' ]
            },
            livereload : {
                options : {
                    livereload : '<%= connect.options.livereload %>'
                },
                files : [ '<%= proj.livereload %>/{,*/}*.html', '<%= proj.livereload %>/css/{,*/}*.css',
                        '<%= proj.livereload %>/js/{,*/}*.js',
                        '<%= proj.livereload %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}' ]
            }
        },

        connect : {
            options : {
                port : 9000,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname : '0.0.0.0',
                livereload : 35729,
            },
            proxies : [ {
                context : [ '/rest' ],
                host : 'localhost',
                port : '8080',
                https : false,
                changeOrigin : true,
                xforward : false
            } ],
            livereload : {
                options : {
                    open : true,
                    base : [ '<%= proj.livereload %>' ],
                    middleware : function(connect, options) {
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
        useminPrepare : {
            html : [ '<%= proj.src %>/index.html', '<%= proj.src %>/content.html', '<%= proj.src %>/map.html',
                    '<%= proj.src %>/admin.html', '<%= proj.src %>/testdata.html' ],
            options : {
                dest : '<%= proj.build %>'
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
        usemin : {
            html : [ '<%= proj.build %>/{,*/}*.html' ],
            options : {
                dirs : [ '<%= proj.build %>' ]
            }
        },
        // Put files not handled in other tasks here
        copy : {
            toTarget : {
                files : [ {
                    expand : true,
                    cwd : '<%= proj.build %>',
                    src : '{,*/}*',
                    dest : '<%= proj.dist %>'
                } ]
            },
            unMod2Build : {
                files : [ {
                    expand : true,
                    cwd : '<%= proj.src %>',
                    src : 'css/ext/{,*/}*.css',
                    dest : '<%= proj.build %>'
                }, {
                    expand : true,
                    cwd : '<%= proj.src %>',
                    src : 'partials/*.html',
                    dest : '<%= proj.build %>'
                } ]
            },
            gen2Build : {
                files : [ {
                    expand : true,
                    cwd : '.tmp/concat',
                    src : '{,*/}*.js',
                    dest : '<%= proj.build %>'
                }, {
                    expand : true,
                    cwd : '.tmp/concat',
                    src : '{,*/}*.css',
                    dest : '<%= proj.build %>'
                } ]
            },
            all2Livereload : {
                files : [ {
                    expand : true,
                    cwd : '<%= proj.src %>',
                    src : '{,**/}*.*',
                    dest : '<%= proj.livereload %>/'
                } ]
            }
        },
        clean : {
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
            server : '<%= proj.build %>'
        },
        replace : {
            run : {
                src : [ '<%= proj.src %>/*.{html,appcache}' ],
                dest : '<%= proj.build %>/', // destination directory or file
                replacements : [ {
                    from : 'js/ext/cdn.cloudflare', // string replacement
                    to : '//cdnjs.cloudflare.com/ajax/libs'
                }, {
                    from : 'js/ext/cdn.googleapis', // string replacement
                    to : '//ajax.googleapis.com/ajax/libs'
                }, {
                    from : 'js/ext/cdn.netdna', // string replacement
                    to : '//netdna.bootstrapcdn.com'
                }, {
                    from : 'css/ext/cdn.netdna', // string replacement
                    to : '//netdna.bootstrapcdn.com'
                }, ]
            }
        },
        cdnify : {
            cdn : {
                options : {
                    rewriter : function(url) {
                        url = url.replace("js/ext/cdn.cloudflare", "//cdnjs.cloudflare.com/ajax/libs");
                        url = url.replace("js/ext/cdn.googleapis", "//ajax.googleapis.com/ajax/libs");
                        url = url.replace("js/ext/cdn.netdna", "//netdna.bootstrapcdn.com");
                        url = url.replace("css/ext/cdn.netdna", "//netdna.bootstrapcdn.com");
                        return url;
                    }
                },
                files : [ {
                    expand : true,
                    cwd : '<%= proj.build %>/webapp',
                    src : '*.{html}',
                    dest : '<%= proj.build %>'
                } ]
            }
        },
        concurrent : {
            server : [ 'concat:dist' ],
            // test : [ 'coffee', 'copy:styles' ],
            build : [ 'copy:styles2Build', 'copy:html2Build' ]
        },
        karma : {
            options : {
                configFile : 'src/test/resources/karma.conf.js',
                browsers : [ 'Chrome', 'Firefox', 'PhantomJS' ],
            },
            continuous : {
                port : 5678,
                singleRun : true,
                browsers : [ 'PhantomJS' ],
            },
            unit : {
                singleRun : false,
                autoWatch : true,
                keepalive : true
            }

        },
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

    grunt.registerTask('server',
            function(target) {
                if (target === 'dist') {
                    return grunt.task.run([ 'build', 'connect:dist:keepalive' ]);
                }

                grunt.task.run([ 'copy:all2Livereload', /* 'autoprefixer', */'configureProxies', 'connect:livereload',
                        'watch' ]);
            });

    // grunt.registerTask('test', [ 'clean:server', 'concurrent:test',
    // 'autoprefixer', 'connect:test', 'karma' ]);

    grunt.registerTask('test', [ 'karma:continuous' ]);

    grunt.registerTask('build', [ 'useminPrepare', 'copy:unMod2Build', 'replace:run', 'concat', 'usemin',
            'copy:gen2Build', 'copy:toTarget' ]);

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