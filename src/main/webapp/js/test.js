/**
 * This file contains test functions and should 
 * not be included to any released projects.     
 */

// Keep count on succeded and failed tests
var succeded = 0;
var failed = 0;

// Run all tests when document is ready
$(document).ready(function() {
	 runTests();
});

/**
 * Runs all the tests.
 * Add test function calls to this function.
 */
function runTests(){
	
	//testTo24hClock();
	
	//printTestResults;
	
}

/**
 * Testing: to24hClock(time)
 */
function testTo24hClock(){
	
	assert(to24hClock("12:00:00 AM"), "00:00:00");
	assert(to24hClock("12:01:00 AM"), "00:01:00");
	assert(to24hClock("12:59:00 AM"), "00:59:00");
	
	assert(to24hClock("01:00:00 AM"), "01:00:00");
	assert(to24hClock("11:00:00 AM"), "11:00:00");
	assert(to24hClock("11:59:00 AM"), "11:59:00");
	
	assert(to24hClock("12:00:00 PM"), "12:00:00");
	assert(to24hClock("12:01:00 PM"), "12:01:00");
	assert(to24hClock("12:59:00 PM"), "12:59:00");
	
	assert(to24hClock("01:00:00 PM"), "13:00:00");
	assert(to24hClock("11:00:00 PM"), "23:00:00");
	assert(to24hClock("11:59:00 PM"), "23:59:00");
	
}

/**
 * Returns true of param test is equal to param assertion.
 * @param test
 * @param assertion
 * @returns {Boolean}
 */
function assert(test, assertion){
	
	if (test == assertion){
		succeded++;
		console.log("Test succeded: " + test + " EQUAL TO " + assertion);
		return true;
	}
	failed++;
	console.log("Test failed: " + test + " NOT EQUAL TO " + assertion);
	return false;
}

/**
 * Prints the test results to the console.
 */
function printTestResults(){
	console.log("------ TEST RESULTS ------");
	console.log(succeded + " tests succeeded");
	console.log(failed + " tests failed");
	console.log("--------------------------");
}
