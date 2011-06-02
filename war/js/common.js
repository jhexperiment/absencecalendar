function querySite(url, data, success_function)
{
	if (isEmpty(data['type'])) {
		data['type'] = 'GET'
	}
	if (isEmpty(data['returnType'])) {
		data['returnType'] = 'json'
	}
	$.ajax({
		'type': data['type'],
		'url': url,
		'data': data,
		'dataType': data['returnType']
	}).success(success_function);
}

function isEmpty(obj) {return (obj == null || obj == '' || $.isEmptyObject(obj));}

function isValidDate(dateString) {
	var date = Date.parse(dateString);
	
	return ! isNaN(date);
}