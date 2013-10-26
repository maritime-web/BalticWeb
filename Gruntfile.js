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
            dist : 'target/webapp'
        },
        watch : {
            js : {
                files : [ '<%= proj.src %>/js/{,*/}*.js' ],
                tasks : [ 'default' ]
            },
            jsTest : {
                files : [ '<%= proj.test %>/{,*/}*.js' ],
                tasks : [ 'test' ]
            },
            // styles: {
            // files: ['<%= proj.src %>/css/{,*/}*.css'],
            // tasks: ['copy:styles', 'autoprefixer']
            // },
            livereload : {
                options : {
                    livereload : '<%= connect.options.livereload %>'
                },
                files : [ '<%= proj.src %>/{,*/}*.html', '<%= proj.src %>/css/{,*/}*.css',
                        '<%= proj.src %>/js/{,*/}*.js', '<%= proj.src %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}' ]
            }
        },

        connect : {
            options : {
                port : 9000,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname : 'localhost',
                livereload : 35729,
                directory : 'arcticweb'
            },
            proxies : [ {
                context : [ '/rest', '/json_proxy' ],
                host : 'localhost',
                port : '8080',
                https : false,
                changeOrigin : true,
                xforward : false,
                rewrite : {
                    '/rest' : '/arcticweb/rest',
                    '/json_proxy' : '/arcticweb/json_proxy',
                }
            } ],
            livereload : {
                options : {
                    open : true,
                    base : [ '<%= proj.src %>' ],
                    middleware : function(connect, options) {
                        return [ proxySnippet, mountFolder(connect, 'src/main/webapp') ];
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
            html : [ '<%= proj.src %>/front.html', '<%= proj.src %>/map.html', '<%= proj.src %>/report.html' ],
            options : {
                dest : '<%= proj.build %>'
//                flow : {
//                    html : {
//                        steps : {
//                            'js' : [ 'concat' ]
//                        },
//                        post : {}
//                    }
//                }
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
            nonJs2Build : {
                files : [ {
                    expand : true,
                    cwd : '<%= proj.src %>',
                    src : 'css/{,*/}*.css',
                    dest : '<%= proj.build %>'
                }, {
                    expand : true,
                    cwd : '<%= proj.src %>',
                    src : '{,*/}*.html',
                    dest : '<%= proj.build %>'
                } ]
            },
            js2Build : {
                files : [ {
                    expand : true,
                    cwd : '.tmp/concat',
                    src : '{,*/}*.js',
                    dest : '<%= proj.build %>'
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
        concurrent : {
            server : [ 'concat:dist' ],
            // test : [ 'coffee', 'copy:styles' ],
            build : [ 'copy:styles2Build', 'copy:html2Build' ]
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

    grunt.registerTask('server', function(target) {
        if (target === 'dist') {
            return grunt.task.run([ 'build', 'connect:dist:keepalive' ]);
        }

        grunt.task.run([ /* 'autoprefixer', */'configureProxies', 'connect:livereload', 'watch' ]);
    });

    // grunt.registerTask('test', [ 'clean:server', 'concurrent:test',
    // 'autoprefixer', 'connect:test', 'karma' ]);

    grunt.registerTask('build', [ 'useminPrepare', 'copy:nonJs2Build', 'concat', 'usemin', 'copy:js2Build', 'copy:toTarget' ]);

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