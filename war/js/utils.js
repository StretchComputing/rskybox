
// Simple RegEx to ensure a valid phone number format.
function validPhoneNumber(phoneNumber) {
  return /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/.test(phoneNumber);
}


// Build a list of mobile carriers options.
//
// carriers: array of id/name pairs
// returns: the generate markup
var NO_CARRIER = '-1';
function carrierOptions(carriers) {
  var markup = '<option value="' + NO_CARRIER + '">Mobile Carrier</option>';
  for (i = 0; i < carriers.length; i++) {
    markup += '<option value="' + carriers[i]['id'] + '">' + carriers[i]['name'] + '</option>';
  }
  return markup;
}
