describe('myFirstTest', function() {
  it('should increment a variable', function () {
    var foo = 0;            // set up the world
    foo++;                  // call your application code
    expect(foo).toEqual(1); // passes because foo == 1
  });
});