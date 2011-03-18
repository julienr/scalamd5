//Fragment
//See http://www.fabiensanglard.net/bumpMapping/index.php

//localTex is bump
uniform sampler2D colorTex;
uniform sampler2D localTex;

varying vec3 lightVec;
varying vec3 eyeVec;

void main () {
  //normal map is encoded as a [0,1] range, so we decompress to [-1,1]
  vec3 normal = 2.0*texture2D(localTex, gl_TexCoord[0].st).rgb - 1.0;
  normal = normalize(normal);

  //ambient 
  gl_FragColor = vec4(0,0,0,1); //TODO: Fetch it from a uniform

  //diffuse
  float NdotL = max(dot(lightVec, normal), 0.0);

  if (NdotL > 0.0) {
    vec4 diffuseMat = texture2D(colorTex, gl_TexCoord[0].st);
    vec4 diffuseLight = vec4(0.2,0.2,0.2,1); //TODO: make uniform

    gl_FragColor += diffuseMat*diffuseLight*NdotL;

    //TODO: specular
  }
}
