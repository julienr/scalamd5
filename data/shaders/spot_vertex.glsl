//Point light Vertex
//See http://www.fabiensanglard.net/bumpMapping/index.php
attribute vec3 tangent;

uniform vec3 lightPos;

varying vec3 tbnLightVec;
varying vec3 eyeLightVec;
varying vec3 eyeVec;
varying float distToLight;

varying vec4 lsVert;

void main () {
  gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
  gl_TexCoord[0] = gl_MultiTexCoord0;

  //calculate light-space vertex
  lsVert = gl_TextureMatrix[7]*gl_Vertex;
  //gl_Position = lsVert;

  //Eye space -> Tangent space transformation matrix
  vec3 n = normalize(gl_NormalMatrix*gl_Normal);
  vec3 t = normalize(gl_NormalMatrix*tangent);
  vec3 b = cross(t, n);

  mat3 tbnMatrix = mat3(t.x, b.x, n.x,
                        t.y, b.y, n.y,
                        t.z, b.z, n.z);

  //get vertex and light position in eye space
  vec3 vertPos = vec3(gl_ModelViewMatrix*gl_Vertex);
  vec3 viewLightPos = lightPos; 
  vec3 aux = viewLightPos-vertPos;
  eyeLightVec = normalize(aux);
  distToLight = length(aux);
  
  tbnLightVec = tbnMatrix*eyeLightVec;

  eyeVec = tbnMatrix*(-vertPos);
}
