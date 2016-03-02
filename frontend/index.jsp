<!DOCTYPE html>
<html>
<head>
	<title>t2k Marketplace</title>
	<link rel="stylesheet" type="text/css" href="css/style.css">
	<script src="node_modules\mustache\mustache.min.js"></script>
	 <!--Import Google Icon Font-->
      <link href="http://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
      <!--Import materialize.css-->
      <link type="text/css" rel="stylesheet" href="node_modules\materialize-css\dist\css\materialize.min.css" media="screen,projection"/>
      <!--Let browser know website is optimized for mobile-->
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
</head>
<body>
	<div id="menu">
		<div id="logo">T2K Marketplace</div>
		<div id="left-menu">
			<a target="_blank" href="http://www.timetoknow.com/" class="left-menu-item"><img src="css/media/logos/t2k.png"></a>
			<a target="_blank" href="http://www.powtoon.com/" class="left-menu-item "><img src="css/media/logos/powtoon.png"></a>
			<a target="_blank" href="http://www.check123.com/" class="left-menu-item"><img src="css/media/logos/check123.png"></a>
			<a target="_blank" href="http://www.teachpitch.com/" class="left-menu-item"><img src="css/media/logos/teachpitch.png"></a>
		</div>
		<div id="right-menu">
			<div class="right-menu-item waves-effect waves btn-flat">Sign  In</div>
			<div class="right-menu-item waves-effect waves btn-flat">Register</div>
		</div>

	</div>
	<div class="background"></div>
	<div id="main">
		<div id="searchBox">
			 <div class="serachIcon z-depth-5"></div> 
			<div class="searchBox-text z-depth-5" contenteditable=true spellcheck=false>  </div>
		</div>
	</div>
	<div id="gallery"></div>

	<footer class="page-footer black">
          <div class="container">
            <div class="row">
              <div class="col l6 s12">
                <h5 class="white-text">T2K Marketplace</h5>
                <p class="grey-text text-lighten-4">have a suggestion? contact us!</p>
              </div>
              <div class="col l4 offset-l2 s12">
                <h5 class="white-text">Read More</h5>
                <ul>
                  <li><a class="grey-text text-lighten-3" href="http://www.timetoknow.com/">About</a></li>
                  <li><a class="grey-text text-lighten-3" href="http://www.timetoknow.com/">Jobs</a></li>
                  <li><a class="grey-text text-lighten-3" href="http://www.timetoknow.com/">Info</a></li>
                  <li><a class="grey-text text-lighten-3" href="http://www.timetoknow.com/">Creers</a></li>
                </ul>
              </div>
            </div>
          </div>
          <div class="footer-copyright">
          		<div class="container">© 2016 T2K Hackaton - Libi Becker, Elad Avidan, Tali Muktar
          		<a class="grey-text text-lighten-4 right" href="http://www.timetoknow.com/">Time To Know</a>
            </div>
          </div>
        </footer>

      <script type="text/javascript" src="https://code.jquery.com/jquery-2.1.1.min.js"></script>
      <script type="text/javascript" src="node_modules/materialize-css/dist/js/materialize.min.js"></script>
	<script type="text/javascript" src="scripts/main.js"></script>
</body>
</html>