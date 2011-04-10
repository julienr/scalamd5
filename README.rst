Scala MD5 Loader
================
This is a MD5 model (Doom 3) loader written in Scala/OpenGL using the LWJGL library.
It supports :

* .md5mesh (version 10) models loading
* .md5anim animations (give as second argument in command line)
* per-pixel lighting with bump mapping and specular highlights
* basic shadow maps using OpenGL framebuffer objects

Here is a screenshot of the application running. In the lower-right corner, the depth buffer from the light point of view is displayed in colored scale. Below it, the color buffer from the light point of view is shown. Not that this color buffer is only useful for debugging and that textures/shaders are disabled when rendering from the light point of view.

.. image:: https://github.com/julienr/scalamd5/raw/master/screen.png 

Usage
=====
To be able to run this program, you must have some md5 models to load. I used Doom 3 models during development, but I cannot redistribute them.
Just give the path to a .md5mesh as the first argument to the program and (optionnaly), the path to a .md5anim as the second argument.

For a mesh with a shader property equal to *models/monsters/imp/imp*, the program will load the following textures in the *data/textures/monsters/imp/* directory:

* imp_d.tga for the diffuse map
* imp_local.tga for the normal map
* imp_s.tga for the specular map

Internals
=========
See the notes.rst file for some observations made during the development.
