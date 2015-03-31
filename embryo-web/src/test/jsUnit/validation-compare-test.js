describe('validation.compare - lteq directive', function () {
    var document;
    var elm; // our directive jqLite element
    var scope; // the scope where our directive is inserted

    beforeEach(function () {
        module('embryo.validation.compare');
    });

    function initialScope($rootScope, value1, value2) {
        scope = $rootScope.$new();
        scope.obj = {
            value1: value1,
            value2: value2
        };
        return scope;
    }

    function changeValues(value1, value2) {
        scope.obj.value1 = value1;
        scope.obj.value2 = value2;

        if (!scope.$$phase) {
            scope.$apply(function () {
            });
        }
    }

    function compileDirective(tpl) {
        // function to compile a fresh directive with the given
        // template, or a default one
        // compile the tpl with the $rootScope created above
        // wrap our directive inside a form to be able to test
        // that our form integration works well (via ngModelController)
        // our directive instance is then put in the global 'elm'
        // variable for further tests
        if (!tpl) {
            tpl = '<form name="form">' +
            '  <input x-ng-model="obj.value1" type="number"/>' +
            '  <input x-ng-model="obj.value2" type="number" name="value2" lteq="obj.value1"/>' +
            '  <span x-ng-if="form.value2.$error.lteq">Value2 must be smaller than or equal to value1</span>' +
            '</form>';
        }

        // inject allows you to use AngularJS dependency injection
        // to retrieve and use other services
        inject(function ($compile, $document) {
            elm = $compile(tpl)(scope);
            angular.element($document.body).append(elm);
            document = $document;
        });
        // $digest is necessary to finalize the directive generation
        scope.$digest();
    }

    describe('initial validation of numbers', function () {
        it('If no initial number2 value, then $error.lteq should not be present', inject(function ($rootScope) {
            initialScope($rootScope, 1, null);
            compileDirective();
            var span = elm.find("span");
            expect(span.length).toEqual(0);
        }));
        it('If initial number2 value less than number1 value then $error.lteq should not be present', inject(function ($rootScope) {
            initialScope($rootScope, 3, 2);
            compileDirective();
            var span = elm.find("span");
            expect(span.length).toEqual(0);
        }));
        it('If initial number2 present but no number1 value $error.lteq should not be present', inject(function ($rootScope) {
            initialScope($rootScope, null, 2);
            compileDirective();
            var span = elm.find("span");
            expect(span.length).toEqual(0);
        }));
        it('If initial number2 is less than initial number1 then $error.lteq should be true', inject(function ($rootScope) {
            initialScope($rootScope, 5, 8);
            compileDirective();
            var span = elm.find("span");

            expect(span.length).toEqual(1);
            expect(span.text()).toBe("Value2 must be smaller than or equal to value1");
        }));
    });

    describe('Modification triggers validation', function () {
        // before each test, creates a new fresh scope
        // the inject function interest is to make use of the angularJS
        // dependency injection to get some other services in our test
        // here we need $rootScope to create a new scope
        beforeEach(inject(function ($rootScope) {
            initialScope($rootScope, null, null);
            compileDirective();
        }));

        it('If number2 is not present, then $error.lteq is also not present', function () {
            var span = elm.find("span");
            expect(span.length).toEqual(0);

            changeValues(2, null)

            var span = elm.find("span");
            expect(span.length).toEqual(0);
        });

        it('If number2 is present but number1 is not, then $error.lteq is also not present', function () {
            var span = elm.find("span");
            expect(span.length).toEqual(0);

            changeValues(null, 2)

            var span = elm.find("span");
            expect(span.length).toEqual(0);
        });

        it('If number2 is less than number1, then $error.lteq is not present', function () {
            var span = elm.find("span");
            expect(span.length).toEqual(0);

            changeValues(20, 1);

            var span = elm.find("span");
            expect(span.length).toEqual(0);
        });

        it('If number2 is greater than number 1, then $error.lteq should be true', function () {
            var span = elm.find("span");
            expect(span.length).toEqual(0);

            changeValues(2, 3);

            var span = elm.find("span");
            expect(span.length).toEqual(1);
            expect(span.text()).toBe("Value2 must be smaller than or equal to value1");
        });
    });

    describe('Validation works with datepicker', function () {
        // before each test, creates a new fresh scope
        // the inject function interest is to make use of the angularJS
        // dependency injection to get some other services in our test
        // here we need $rootScope to create a new scope
        beforeEach(inject(function ($rootScope) {
            var tpl = '<form name="form">' +
                '  <datetimepicker x-ng-model="obj.value1" />' +
                '  <datetimepicker x-ng-model="obj.value2" name="value2" lteq="obj.value1"/>' +
                '  <span x-ng-if="form.value2.$error.lteq">Value2 must be smaller than or equal to value1</span>' +
                '</form>';
            initialScope($rootScope, null, null);
            compileDirective(tpl);
        }));

        it('If value2 is greater than value1, then $error.lteq should be true', function () {
            var span = elm.find("span");
            expect(span.length).toEqual(0);

            changeValues(1427800860000, 1427887260000);

            var span = elm.find("span");
            expect(span.length).toEqual(1);
            expect(span.text()).toBe("Value2 must be smaller than or equal to value1");
        });
    });
});
