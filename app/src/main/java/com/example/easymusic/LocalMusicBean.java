package com.example.easymusic;

public class LocalMusicBean {
    private String id;      //歌曲id
    private String song;    //歌曲名称
    private String singer;  //歌手名称
    private String album;   //专辑名称
    private long duration;  //歌曲时长
    private String path;    //歌曲路径

    public LocalMusicBean() {
    }

    public LocalMusicBean(String id, String song, String singer, String album, long duration, String path) {
        this.id = id;
        this.song = song;
        this.singer = singer;
        this.album = album;
        this.duration = duration;
        this.path = path;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
