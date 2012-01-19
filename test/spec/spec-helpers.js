beforeEach(function() {
  this.validResponse = function(responseText, httpStatus) {
    return [
      httpStatus || 200,
      {"Content-Type": "application/json"},
      JSON.stringify(responseText)
    ];
  };
});
