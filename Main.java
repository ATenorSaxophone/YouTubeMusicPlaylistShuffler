package com.lonestarbandit;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.Random;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static void main(String[] args) throws Exception {
        YouTube youtubeService = Auth.authorize();
        Scanner scanner = new Scanner(System.in);

        ArrayList<Playlist> playlists = listUserPlaylists(youtubeService);
        ArrayList<String> desiredPlaylistsInputs = new ArrayList<>();
        ArrayList<Playlist> truePlaylists;

        //Ask for desired playlists
        boolean listing = true;
        System.out.println("Type 'Break' when done!\nWhat playlists do you want shuffled?");

        while(listing){
            String input = scanner.nextLine();
            if(input.equalsIgnoreCase("break")){
                listing = false;
                System.out.println("Generating playlist...");
            }else{
                desiredPlaylistsInputs.add(input);
            }
        }

        //get the desired playlists
        truePlaylists = intersectPlaylists(playlists, desiredPlaylistsInputs);

        // Add add videos randomly into playlist
        createShuffledPlaylist(truePlaylists, youtubeService);

        //System.out.println("Merged playlist created successfully!);
        System.out.println("Merged playlist created successfully!");

    }
    public static ArrayList<Playlist> listUserPlaylists(YouTube youtubeService) throws Exception {
        String nextPageToken = null;
        ArrayList<Playlist> returnPlaylists = new ArrayList<>();

        do {
            YouTube.Playlists.List request = youtubeService.playlists()
                    .list("snippet,id")
                    .setMine(true)
                    .setMaxResults(25L)
                    .setPageToken(nextPageToken);

            PlaylistListResponse response = request.execute();
            List<Playlist> playlists = response.getItems();

            for (Playlist playlist : playlists) {
                returnPlaylists.add(playlist);

            }

            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        return returnPlaylists;
    }

    public static ArrayList<PlaylistItem> listUserPlaylistSongs(YouTube youtubeService, Playlist playlist) throws Exception{
        String nextPageToken = null;
        ArrayList<PlaylistItem> videos = new ArrayList<>();

        do {
            YouTube.PlaylistItems.List request = youtubeService.playlistItems().list("snippet,id")
                    .setPlaylistId(playlist.getId())
                    .setMaxResults(100L)
                    .setPageToken(nextPageToken);

            PlaylistItemListResponse response = request.execute();
            List<PlaylistItem> playlistItems = response.getItems();

            for(PlaylistItem pLI : playlistItems){

                videos.add(pLI);
            }
        }while (nextPageToken != null);

        return videos;
    }

    public static ArrayList<Playlist> intersectPlaylists(ArrayList<Playlist> playlists, ArrayList<String> desiredPlaylists){
        ArrayList<Playlist> truePlaylists = new ArrayList<>();

        for(Playlist playlist : playlists) {
            for (String title : desiredPlaylists) {
                if (playlist.getSnippet().getTitle().equalsIgnoreCase(title)) truePlaylists.add(playlist);

            }
        }

        return truePlaylists;
    }

    public static Playlist createPlaylist(String status, YouTube youtubeService) throws IOException {
        Playlist playlist = new Playlist();
        PlaylistSnippet snippet = new PlaylistSnippet();

        snippet.setTitle("Shuffled Playlist(Test)");
        playlist.setSnippet(snippet);

        playlist.setStatus(new PlaylistStatus().setPrivacyStatus(status));

        YouTube.Playlists.Insert request = youtubeService.playlists().insert("snippet,status", playlist);
        Playlist response = request.execute();

        return response;
    }

    public static void createShuffledPlaylist(ArrayList<Playlist> playlists, YouTube youtubeService) throws Exception {

        Random rand = new Random();

        ArrayList<PlaylistItem> shuffledSongs = new ArrayList<>();

        ArrayList<ArrayList<PlaylistItem>> shuffler = new ArrayList<>();
        ArrayList<PlaylistItem> items;

        for(Playlist playlist : playlists){
            items = listUserPlaylistSongs(youtubeService, playlist);
            shuffler.add(items);
        }

        while(shuffledSongs.size() < 100 && !shuffler.isEmpty()){
            int r1 = rand.nextInt(shuffler.size());
            int r2 = rand.nextInt(shuffler.get(r1).size());

            shuffledSongs.add(shuffler.get(r1).get(r2));
            shuffler.get(r1).remove(r2);

            if(shuffler.get(r1).isEmpty()){
                shuffler.remove(r1);
            }
        }

        Playlist playlist = createPlaylist("private", youtubeService);

        PlaylistItemSnippet shuffledSnippet = new PlaylistItemSnippet();
        shuffledSnippet.setPlaylistId(playlist.getId());
        shuffledSnippet.setPosition(0L);

        for(PlaylistItem item : shuffledSongs){

            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(item.getSnippet().getResourceId().getVideoId());

            shuffledSnippet.setResourceId(resourceId);
            item.setSnippet(shuffledSnippet);

            YouTube.PlaylistItems.Insert request = youtubeService.playlistItems().insert("snippet", item);
            try {
                PlaylistItem response = request.execute();
            }catch (Exception e){
                System.out.println(e.getMessage() + "\nSong: " + item.getSnippet().getTitle());
            }


        }
    }
}