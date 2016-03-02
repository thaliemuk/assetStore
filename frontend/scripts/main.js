$(document).ready(function () {
	$.get('html\\gallery.html', function(template) {
       var data = {
            galleryData :[
               {
                    "thumbnail" :"css/media/IMAGES/1 (1).jpg",
                    "name": "1",
                    "source":"t2k",
                    "price": null,
                    "tags":"bloop,chart",
                    "isVideo":true

               },{
                    "thumbnail" :"css/media/IMAGES/1 (2).jpg",
                    "name": "2",
                    "source":"powtoon",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false
               },{
                    "thumbnail" :"css/media/IMAGES/1 (3).jpg",
                    "name": "3",
                    "source":"teachpitch",
                    "price":null,
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true

               },{
                    "thumbnail" :"css/media/IMAGES/1 (4).jpg",
                    "name": "1",
                    "source":"check123",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false
               },{
                    "thumbnail" :"css/media/IMAGES/1 (5).jpg",
                    "name": "2",
                    "source":"powtoon",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true
               },{
                    "thumbnail" :"css/media/IMAGES/1 (6).jpg",
                    "name": "3",
                    "source":"t2k",
                    "price":"2.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false

               },{
                    "thumbnail" :"css/media/IMAGES/1 (7).jpg",
                    "name": "1",
                    "source":"check123",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true

               },{
                    "thumbnail" :"css/media/IMAGES/1 (8).jpg",
                    "name": "2",
                    "source":"teachpitch",
                    "price":"0.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false
               },{
                    "thumbnail" :"css/media/IMAGES/1 (9).jpg",
                    "name": "3",
                    "source":"check123",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false
               },{
                    "thumbnail" :"css/media/IMAGES/1 (10).jpg",
                    "name": "1",
                    "source":"powtoon",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false
               },{
                    "thumbnail" :"css/media/IMAGES/1 (11).jpg",
                    "name": "2",
                    "source":"teachpitch",
                    "price":null,
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true
               },{
                    "thumbnail" :"css/media/IMAGES/1 (12).jpg",
                    "name": "3",
                    "source":"check123",
                    "price":"10.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true

               },{
                    "thumbnail" :"css/media/IMAGES/1 (13).jpg",
                    "name": "1",
                    "source":"t2k",
                    "price":"5.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true
               },{
                    "thumbnail" :"css/media/IMAGES/1 (14).jpg",
                    "name": "2",
                    "source":"teachpitch",
                    "price":"4.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":true
               },{
                    "thumbnail" :"css/media/IMAGES/1 (15).jpg",
                    "name": "3",
                    "source":"t2k",
                    "price":"3.99$",
                    "tags":"animal,bloop,chart,balloon",
                    "isVideo":false

               }

           ]
       };

	   var galleryHtml = Mustache.render(template,data);
	   $("#gallery").html(galleryHtml);
    });

	$(".serachIcon").on('click',function(){
		var textToSearch = $(".searchBox-text").text();
	});

    setInterval(function(){ 
        var imageToDisplayIndex = Math.floor(Math.random() * 10) + 1 ; 
        console.log(imageToDisplayIndex);
        $(".background").css({'background-image':'url("css/media/backgrounds/'+imageToDisplayIndex+'.jpg")'});
    }, 3000);
});

	