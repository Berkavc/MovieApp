package com.task.movieapp.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.task.movieapp.R
import com.task.movieapp.common.base.BaseFragment
import com.task.movieapp.domain.model.ResultData
import dagger.hilt.android.AndroidEntryPoint
import com.task.movieapp.databinding.FragmentMovieBinding
import timber.log.Timber
import androidx.appcompat.widget.SearchView
import com.task.movieapp.common.utils.clickWithThrottle
import com.task.movieapp.common.utils.sendToHyperLink
import com.task.movieapp.common.utils.shareUrl
import com.task.movieapp.data.MOVIE_DETAIL_URL

@AndroidEntryPoint
class MovieFragment : BaseFragment<MovieViewModel, FragmentMovieBinding>() {
    override val viewModel: MovieViewModel by viewModels()
    override var layoutRes: Int = R.layout.fragment_movie

    private lateinit var adapterMovieRecyclerView: MovieRecyclerViewAdapter

    override fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner) {
            when (it) {
                is ResultData.Success -> {
                    binding.buttonMovieTryAgain.visibility = View.GONE
                    Timber.e("data_success")
                    it.data?.let {
                        if (it.size > 0) {
                            adapterMovieRecyclerView.updateDataSourceWithSearch(it)
                        }
                    }
                }
                is ResultData.Loading -> {
                    Timber.e("data_loading")
                }
                is ResultData.Failed -> {
                    binding.buttonMovieTryAgain.visibility = View.VISIBLE
                    Timber.e("data_failed")
                }
            }
        }

    }

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fetchMovies()
    }

    override fun arrangeUI() {
        binding.searchViewMovie.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterMovieRecyclerView.filter.filter(newText)
                return false
            }

        })
        arrangeMoviesRecyclerView()
        binding.buttonMovieTryAgain.clickWithThrottle {
            viewModel.fetchMovies()
        }
    }

    private fun arrangeMoviesRecyclerView() {
        activity?.let { act ->
            adapterMovieRecyclerView = MovieRecyclerViewAdapter(
                act,
                mutableListOf()
            )
            with(binding.recyclerViewMovies) {
                adapter = adapterMovieRecyclerView
                layoutManager = LinearLayoutManager(act, RecyclerView.VERTICAL, false)
                adapterMovieRecyclerView.onItemClick = { position, item ->
                    navigateToNextFragment(MovieFragmentDirections.actionMovieToMovieDetail(result = item))
                }
                adapterMovieRecyclerView.onItemClickSendHyperLink = { position, item ->
                    sendToHyperLink(act, item.id.toString(), MOVIE_DETAIL_URL)
                }
                adapterMovieRecyclerView.onItemClickShare = { position, item ->
                    shareUrl(
                        act,
                        act.resources.getString(R.string.movie_share_with),
                        act.resources.getString(
                            R.string.movie_share_content,
                            MOVIE_DETAIL_URL + item.id.toString()
                        )
                    )
                }
            }
        }
    }
}