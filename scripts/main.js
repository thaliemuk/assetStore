$(document).ready(function () {
	$.get('html\\gallery.html', function(template) {
       var data = {
            galleryData :[
               {
                    "thumbnail" : "css/media/1.jpg",
                    "name": "1",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/2.jpg",
                    "name": "2",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/3.jpg",
                    "name": "3",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/1.jpg",
                    "name": "1",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/2.jpg",
                    "name": "2",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/3.jpg",
                    "name": "3",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/1.jpg",
                    "name": "1",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/2.jpg",
                    "name": "2",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/3.jpg",
                    "name": "3",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/1.jpg",
                    "name": "1",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/2.jpg",
                    "name": "2",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/3.jpg",
                    "name": "3",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/1.jpg",
                    "name": "1",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/2.jpg",
                    "name": "2",
                    "source":"check123",

               },{
                    "thumbnail" : "css/media/3.jpg",
                    "name": "3",
                    "source":"check123",

               }

           ]
       };

	   var galleryHtml = Mustache.render(template,data);
	   $("#gallery").html(galleryHtml);
});

	$(".serachIcon").on('click',function(){
		var textToSearch = $(".searchBox-text").text();
	});
});

	