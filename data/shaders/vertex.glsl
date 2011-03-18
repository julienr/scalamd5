//Vertex
//See http://www.fabiensanglard.net/bumpMapping/index.php
attribute vec3 tangent;

uniform vec3 lightPos;

varying vec3 lightVec;
varying vec3 eyeVec;

void main () {
  gl_TexCoord[0] = gl_MultiTexCoord0;
  gl_Position = ftransform();

  //Eye space -> Tangent space transformation matrix
  vec3 n = normalize(gl_NormalMatrix*gl_Normal);
  vec3 t = normalize(gl_NormalMatrix*tangent);
  vec3 b = cross(n, t);

  //get vertex and light position in view space
  vec3 vertPos = vec3(gl_ModelViewMatrix*gl_Vertex);
  vec3 viewLightPos = vec3(gl_ModelViewMatrix*vec4(lightPos,1));
  vec3 lightDir = normalize(viewLightPos-vertPos);

  //transform lightdir to tangent space
  vec3 v;
  v.x = dot(lightDir, t);
  v.y = dot(lightDir, b);
  v.z = dot(lightDir, n);
  lightVec = normalize(v);

  //eye vec to tangent space
  v.x = dot(vertPos, t);
  v.y = dot(vertPos, b);
  v.z = dot(vertPos, n);
  eyeVec = normalize(v);

//  gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
}
