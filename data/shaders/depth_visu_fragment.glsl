uniform sampler2D shadowMap;

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
  float depth = texture2D(shadowMap, gl_TexCoord[0].st).r;
  //The pow is just here to strengtenth the depth differences
  gl_FragColor = vec4(toColorScale(pow(depth,30)),1);
  //gl_FragColor = vec4(vec3(pow(depth,4)), 1);
  //gl_FragColor = texture2D(shadowMap, gl_TexCoord[0].st);
}

