//Point light Fragment
//See http://www.fabiensanglard.net/bumpMapping/index.php

//localTex is bump
uniform sampler2D colorTex;
uniform sampler2D localTex;
uniform sampler2D specularTex;
uniform sampler2D shadowMap;

//x = constant attenuation
//y = linear attenuation
//z = quadratic attenuation
uniform vec3 attVector;

uniform float spotCosCutoff;
uniform float spotExp;

uniform vec3 eyeSpotDir;

//light vec is the vector going from the light to the vertex
varying vec3 tbnLightVec;
varying vec3 eyeLightVec;
varying vec3 eyeVec;
varying float distToLight;

varying vec4 lsVert;

//Returns true if the current fragment is in the shadow
bool isShadowed () {
  //shadowCoord is in homogoneous coords, need to divide by w to project it on the light view
  vec4 lsVertProj = lsVert/lsVert.w;
  float depth = texture2D(shadowMap, lsVertProj.st).r;
  if (lsVert.w > 0.0) {
    return lsVertProj.z > depth;
  } else { //if w == 0, point is not visible
    return false;
  }
}

//transform a [0,1] value to a BGR (blue = small, red = big) colored scale
vec3 toColorScale (float v) {
  const float midpoint = 0.5f;
  vec3 color = vec3(0.0f,0.0f,0.0f);
  //R
  if (v > midpoint)
    color.r = 0.0f;
  else
    color.r = 1.0f-2.0f*v;

  //G
  if (v < midpoint)
    color.g = 2.0f*v;
  else
    color.g = 1.0f-2.0f*(v-0.5f);

  //B
  if (v < midpoint)
    color.b = 0.0f;
  else
    color.b = 2.0f*(v-0.5f);

  return color;
}

void main () {
  //normal map is encoded as a [0,1] range, so we decompress to [-1,1]
  vec3 normal = 2.0*texture2D(localTex, gl_TexCoord[0].st).rgb - 1.0;

  //ambient 
  gl_FragColor = vec4(0,0,0,1); //TODO: Fetch it from a uniform

  //diffuse
  vec3 l = normalize(tbnLightVec);
  float NdotL = max(dot(normal, l), 0.0);

  if (NdotL > 0.0) {
    float spotEffect = dot(normalize(eyeSpotDir), normalize(-eyeLightVec));
    if (spotEffect > spotCosCutoff) {
      spotEffect = pow(spotEffect, spotExp);
      float att = spotEffect/(attVector.x + attVector.y*distToLight + attVector.z*distToLight*distToLight);
      //diffuse
      vec4 diffuseMat = texture2D(colorTex, gl_TexCoord[0].st);
      vec4 diffuseLight = vec4(1,1,1,1); //TODO: make uniform
      gl_FragColor += att*(diffuseMat*diffuseLight*NdotL);

      //specular
      //compute half vector as 
      vec3 hv = normalize(eyeVec - l);
      float NdotHV = max(dot(normal, hv), 0.0);
      vec4 specular = texture2D(specularTex, gl_TexCoord[0].st);
      //gl_FragColor = vec4(vec3(NdotHV), 1);
      float shininess = 50.0f;
      vec4 lightSpecular = vec4(10,10,10,1);
      gl_FragColor += att*(lightSpecular*specular*pow(NdotHV, shininess));

    //gl_FragColor = vec4(vec3(att),1);
    }
  }

/*  if (isShadowed())
    gl_FragColor *= 0.5;*/

  /*vec4 lightVertex = lsVert/lsVert.w;
  float depth = texture2D(shadowMap, lightVertex.st).r;
  //DEBUG: should display the vertex depth when viewed from the light => same color as depth map visualization
  gl_FragColor = vec4(toColorScale(pow(depth, 30)), 1);*/
}
