package com.example.administrator.picturetobase64;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Administrator on 2018/4/16/016.
 */

public class App {
    String text;
    private List<Str> face;
    public String getText()
    {
        return text;
    }

    public List<Str> getFace() {
        return face;
    }

    public void setId(String text ){this.text=text;}

    public class Str {
        private Attribute attribute;   //Attribute中包含多个属性，故声明为一个内部类，见下方
        private String face_id;
        private Position postion;
        private String tag;

        public String getFace_id() {
            return face_id;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public Position getPostion() {
            return postion;
        }

        public String getTag() {
            return tag;
        }

        public class Attribute {
            private Age age;
            private Gender gender;
            private Glass glass;
            private Pose pose;
            private Race race;
            private Smiling smiling;

            public Age getAge() {
                return age;
            }

            public Gender getGender() {
                return gender;
            }

            public Glass getGlass() {
                return glass;
            }

            public Pose getPose() {
                return pose;
            }

            public Race getRace() {
                return race;
            }

            public Smiling getSmiling() {
                return smiling;
            }

            public class Age {
                private int range;
                private int value;

                public int getRange() {
                    return range;
                }

                public int getValue() {
                    return value;
                }
            }

            public class Gender {
                private double confidence;
                private String value;

                public double getConfidence() {
                    return confidence;
                }

                public String getValue() {
                    return value;
                }
            }

            public class Glass {
                //同Age理，省略
            }

            public class Pose {
                //同Age理，省略
            }

            public class Race {
                //同Age理，省略
            }

            public class Smiling {
                //同Age理，省略
            }
        }

        public class Position {
            //同Attribute理，省略
        }
    }

}
