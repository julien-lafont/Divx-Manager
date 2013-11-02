'use strict'

angular.module('app.mediatheque')
  .filter('fileicon', [function factory() {
    return function(file) {
      var icone = "file.png"

      if (file.extension == "avi" || file.extension == "mkv") icone = "file-avi.png"
      else if (file.extension == "mpg" || file.extension == "mp4") icone = "file-mp4.png"
      else if (file.extension == "mp3") icone = "file-mp3.png"
      else if (file.extension == "mov") icone = "file-mov.png"
      else if (!file.isFile) icone = "folder.png"

      return "/assets/images/icones/"+ icone
    }
  }])

