package com.alfrsms.challange5.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.alfrsms.challange5.R
import com.alfrsms.challange5.data.network.model.detail.DetailMovie
import com.alfrsms.challange5.data.network.model.detail.Genre
import com.alfrsms.challange5.data.network.model.detail.SpokenLanguage
import com.alfrsms.challange5.databinding.FragmentDetailBinding
import com.alfrsms.challange5.di.MovieServiceLocator
import com.alfrsms.challange5.utils.viewModelFactory
import com.alfrsms.challange5.wrapper.Resource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModelFactory {
        DetailViewModel(MovieServiceLocator.provideMovieRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
    }

    private fun observeData() {
        viewModel.detailResult.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading<*> -> {
                    setLoadingState(true)
                    setErrorState(false)
                }
                is Resource.Error<*> -> {
                    setLoadingState(true)
                    setErrorState(true, it.exception.toString())
                }
                is Resource.Success<*> -> {
                    setLoadingState(false)
                    setErrorState(false)
                    setView(it.payload)
                }
                else -> {}
            }
        }
    }

    private fun setErrorState(isError: Boolean, message: String? = "") {
        binding.tvError.isVisible = isError
        binding.tvError.text = message
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.pbHomeList.isVisible = isLoading
        binding.constraintLayout.isVisible = !isLoading
    }

    private fun setView(movie: DetailMovie?) {
        movie?.let {
            binding.apply {
                Glide.with(this@DetailFragment)
                    .load(IMAGE_URL + movie.backdropPath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivMovieImage)
                tvMovieTitle.text = movie.title
                tvOverview.text = movie.overview
                tvTagline.text = movie.tagline
                tvRating.text = movie.voteAverage?.toFloat().toString()
                tvOfficialWeb.text = movie.homepage


                val genre = setGenre(movie.genres)
                val language = setLanguage(movie.originalLanguage, movie.spokenLanguages)
                tvGenreLangRelease.text = getString(R.string.title_genre_lang_release, genre, language, movie.releaseDate)
            }
        }
    }

    private fun setLanguage(oriLang: String?, spokenLang: List<SpokenLanguage?>?): String? {
        var language: String? = ""
        spokenLang?.let {
            for (i in it) {
                i?.let {
                    if (i.iso6391 == oriLang) {
                        language = i.englishName
                    } }
            }
        }
        return language
    }

    private fun setGenre(genres: List<Genre?>?): String {
        val genre = StringBuilder()
        var separator = ""
        genres?.let {
            for (i in genres) {
                genre.append(separator).append(i?.name)
                separator = ", "
            }
        }
        return genre.toString()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val IMAGE_URL = "https://image.tmdb.org/t/p/w500"
    }
}