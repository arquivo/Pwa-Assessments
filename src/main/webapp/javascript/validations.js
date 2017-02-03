function isValidRadio(radio,msg) {
    var valid = false;
    for (var i = 0; i < radio.length; i++) {
        if (radio[i].checked) {
            return true;
        }
    }
    alert(msg);
    return false;
}

function validateForm(form,msg) {
	if (isValidRadio(form.relevance,msg)) {
		return true;
	}
	return false;
}

