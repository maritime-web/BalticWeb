describe('e-left-bar-x directives', function() {

    var elm, // our directive jqLite element
    scope; // the scope where our directive is inserted

    beforeEach(function() {
        var mockSubject = {
                role : "Shore",
                loggedIn : false,
                isLoggedIn : function() {
                    return this.loggedIn;
                },
                authorize : function(requiredRole){
                    return this.role === requiredRole;
                }
            };
        
//        module('embryo.authentication.service', function($provide) {
//            $provide.value('Subject', mockSubject);
//        });
        module('embryo.control');

    });
    beforeEach(module('ui.bootstrap.accordion'));
    beforeEach(module('template/accordion/accordion.html'));
    beforeEach(module('template/accordion/accordion-group.html'));

    // before each test, creates a new fresh scope
    // the inject function interest is to make use of the angularJS
    // dependency injection to get some other services in our test
    // here we need $rootScope to create a new scope
    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.userName = 'Jesper';
    }));

    function compileDirective(tpl) {
        // function to compile a fresh directive with the given template, or
        // a
        // default one
        // compile the tpl with the $rootScope created above
        // wrap our directive inside a form to be able to test
        // that our form integration works well (via ngModelController)
        // our directive instance is then put in the global 'elm' variable
        // for
        // further tests
        if (!tpl) {
            tpl = '<div><div x-requires-authenticated><span id="requiresLogin">{{userName}}</span></div></div>';
        }

        // inject allows you to use AngularJS dependency injection
        // to retrieve and use other services
        inject(function($compile) {
            elm = $compile(tpl)(scope);
        });
        // $digest is necessary to finalize the directive generation
        scope.$digest();
    }

    describe('e-left-bar directive', function() {
        beforeEach(function() {
            compileDirective('<div e-left-bar accordion><div accordion-group><span id="requiresLogin">{{userName}}</span></div></div>');
        });

        // a single test example, check the produced DOM
//        it('child elements are not in DOM if not logged in', function() {
//            console.log(elm);
//            expect(elm.find('span').length).toEqual(0);
//            expect(elm.find('#requiresLogin').length).toEqual(0);
//        });

        // a single test example, check the produced DOM
//        it('child elements are in DOM if logged in', inject(function(Subject) {
//            Subject.loggedIn = true;
//            scope.$digest();
//
//            expect(elm.find('span').length).toEqual(1);
//            expect(elm.find('#requiresLogin').length).toEqual(1);
//        }));
    });

});
