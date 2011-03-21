//Fragment
//See http://www.fabiensanglard.net/bumpMapping/index.php

//localTex is bump
uniform sampler2D colorTex;
uniform sampler2D localTex;
uniform sampler2D specularTex;

varying vec3 lightVec;
varying vec3 eyeVec;

void main () {
  //normal map is encoded as a [0,1] range, so we decompress to [-1,1]
  vec3 normal = 2.0*texture2D(localTex, gl_TexCoord[0].st).rgb - 1.0;

  //ambient 
  gl_FragColor = vec4(0,0,0,1); //TODO: Fetch it from a uniform

  //diffuse
  vec3 l = normalize(lightVec);
  float NdotL = max(dot(normal, l), 0.0);

  //gl_FragColor = vec4(normal, 1);
  //gl_FragColor = vec4(vec3(NdotL), 1);
  //gl_FragColor = vec4(l, 1);

  if (NdotL > 0.0) {
    vec4 diffuseMat = texture2D(colorTex, gl_TexCoord[0].st);
    vec4 diffuseLight = vec4(1,1,1,1); //TODO: make uniform

    gl_FragColor += diffuseMat*diffuseLight*NdotL;

    //TODO: specular

    //compute half vector as 
    vec3 hv = normalize(eyeVec - l);
    float NdotHV = max(dot(normal, hv), 0.0);
    vec4 specular = texture2D(specularTex, gl_TexCoord[0].st);
    //gl_FragColor = vec4(vec3(NdotHV), 1);
    float shininess = 50.0f;
    vec4 lightSpecular = vec4(10,10,10,1);
    gl_FragColor += lightSpecular*specular*pow(NdotHV, shininess);
  }
}
