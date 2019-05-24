app.service("uploadService",function($http){
	
	this.uploadFile = function(){
		console.log("1111111前端service层");
		// 向后台传递数据:
		var formData = new FormData();
		// 向formData中添加数据:
		formData.append("file",file.files[0]);
		console.log(22222222,formData);
		return $http({
			method:'post',
			url:'/upload/uploadFile.do',
			data:formData,
			headers:{'Content-Type':undefined} ,// Content-Type : text/html  text/plain
			transformRequest: angular.identity
		});
	}
	
});