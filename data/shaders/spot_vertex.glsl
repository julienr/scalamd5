//Point light Vertex
//See http://www.fabiensanglard.net/bumpMapping/index.php
attribute vec3 tangent;

uniform vec3 eyeLightPos;
uniform vec3 eyeSpotDir;
uniform vec3 eyeLightLookAt;

uniform float spotCosCutoff;

varying vec3 tbnLightVec;
varying vec3 eyeLightVec;
varying vec3 tbnVertPos;
varying float distToLight;

varying vec4 lsVert;

//FIXME: Just for debug => remove
varying vec3 dtangent;

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

  //dtangent = vec3(normalize(gl_LightSource[0].position));
  dtangent = vec3((spotCosCutoff+1.0f)/2.0f);

  mat3 tbnMatrix = mat3(t.x, b.x, n.x,
                        t.y, b.y, n.y,
                        t.z, b.z, n.z);

  //get vertex and light position in eye space
  vec3 eyeVertPos = vec3(gl_ModelViewMatrix*gl_Vertex);
  vec3 aux = eyeLightPos-eyeVertPos;
  eyeLightVec = /*normalize(*/aux/*)*/;
  distToLight = length(aux);
  
  //tbnLightVec = tbnMatrix*eyeLightVec;
  tbnLightVec = tbnMatrix*(-normalize(eyeSpotDir));

  tbnVertPos = tbnMatrix*(-eyeVertPos);
}
