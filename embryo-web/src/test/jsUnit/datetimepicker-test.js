describe(
        'datetimepicker directive',
        function() {
            var document;
            var elm; // our directive jqLite element
            var scope; // the scope where our directive is inserted

            beforeEach(function() {
                module('embryo.datepicker');
            });

            function compileDirective(tpl) {

                // function to compile a fresh directive with the given
                // template, or
                // a
                // default one
                // compile the tpl with the $rootScope created above
                // wrap our directive inside a form to be able to test
                // that our form integration works well (via ngModelController)
                // our directive instance is then put in the global 'elm'
                // variable
                // for
                // further tests
                if (!tpl) {
                    tpl = '<div><p><datetimepicker id="date" x-ng-model="obj.date"/></p><input class="dummy" type="text" value=""></input></div>';
                }

                // inject allows you to use AngularJS dependency injection
                // to retrieve and use other services
                inject(function($compile, $document) {
                    elm = $compile(tpl)(scope);
                    angular.element($document.body).append(elm);
                    document = $document;
                });
                // $digest is necessary to finalize the directive generation
                scope.$digest();
            }

            // before each test, creates a new fresh scope
            // the inject function interest is to make use of the angularJS
            // dependency injection to get some other services in our test
            // here we need $rootScope to create a new scope
            beforeEach(inject(function($rootScope, $compile) {
                scope = $rootScope.$new();
                scope.obj = {
                    date : null
                };
                compileDirective();
            }));

            afterEach(function() {
                $(document).find('div.bootstrap-datetimepicker-widget').remove();
            });

            describe('initialization', function() {
                // a single test example, check the produced DOM
                it('DOM is initialized correctly', function() {
                    var div = elm.find("div");
                    expect(div.length).toEqual(1);
                    expect(div.attr("id")).toEqual("date");
                    expect(div.attr("x-ng-model")).toEqual("obj.date");
                    expect(div.find("span").length).toEqual(2);
                    expect(div.find("input").length).toEqual(1);
                    expect(div.find("input").val()).toEqual("");
                    expect($(document).find("div.bootstrap-datetimepicker-widget").length).toEqual(1);
                    expect($(document).find("div.bootstrap-datetimepicker-widget").hasClass("bottom")).toBe(false);

                });

                it('Date popup is opened correctly', function() {
                    expect($(document).find("div.bootstrap-datetimepicker-widget").length).toEqual(1);
                    expect($(document).find("div.bootstrap-datetimepicker-widget").hasClass("bottom")).toBe(false);

                    $(elm.find("span").get(0)).click();

                    expect($(document).find("div.bootstrap-datetimepicker-widget").length).toEqual(1);
                    expect($(document).find("div.bootstrap-datetimepicker-widget").hasClass("bottom")).toBe(true);
                    expect($(document).find("div.bootstrap-datetimepicker-widget").css("display")).toEqual("block");
                });
            });

            describe('no initial model value', function() {
                // a single test example, check the produced DOM

                it('model value is set even though default time (current) is set', function() {
                    expect(scope.obj.date).toEqual(null);
                    expect(elm.find("input.input-sm").val()).toEqual("");

                    // open popup
                    $(elm).find("span.input-group-addon").click();
                    expect($(elm).find("input.input-sm").val()).toMatch(/\d{4}-\d{2}-\d{2} \d{2}:\d{2}/);

                    // closes popup
                    $(elm).find("div.input-group").blur();
                    var now = Date.now();
                    var minute = 3600000;
                    //arrgghh why does below not work?
                    expect(scope.obj.date).not.toBe(null);
                    expect(Math.abs(adjustDateForUTC(scope.obj.date) - now) < minute).toBe(true);
                });
            });
        });
