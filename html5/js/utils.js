// Clear the cookie and redirect to the home page
function logout() {
  document.cookie = document.cookie + ";expires=" + new Date().toGMTString();
  window.location.reload(true);
}

// Simple RegEx to ensure a valid phone number format.
function validPhoneNumber(phoneNumber) {
  return /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/.test(phoneNumber);
}

function validEmailAddress(emailAddress) {
  return /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/.test(emailAddress);
}

// Build a list of mobile carriers options.
//
// carriers: array of id/name pairs
// returns: the generate markup
var NO_CARRIER = '-1';
function carrierOptions(carriers) {
  var markup = '<option value="' + NO_CARRIER + '">Mobile Carrier</option>';
  for (i = 0; i < carriers.length; i++) {
    markup += '<option value="' + carriers[i].id + '">' + carriers[i].name + '</option>';
  }
  return markup;
}


function enableSmsDetails(enabled) {
  $('#phoneNumber').prop('disabled', !enabled);
  $('#mobileCarrierId').prop('disabled', !enabled).selectmenu('refresh');
}

function validateSms() {
  if (!$('#sendSmsNotifications').prop('checked')) { return true; }

  if (!validPhoneNumber($('#phoneNumber').val())) {
    alert('Please enter a valid phone number.');
    return false;
  }
  if ($('#mobileCarrierId').val() == NO_CARRIER) {
    alert('Please select a mobile carrier.');
    return false;
  }

  return true;
}