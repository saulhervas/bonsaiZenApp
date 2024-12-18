package com.bonsaizen.bonsaizenapp.ui.bonsais

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bonsaizen.bonsaizenapp.R
import com.bonsaizen.bonsaizenapp.databinding.FragmentBonsaiListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BonsaiListFragment : Fragment() {

    private lateinit var binding: FragmentBonsaiListBinding
    private lateinit var adapter: BonsaiAdapter
    private val viewModel: BonsaiListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBonsaiListBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        observeViewModel()
        setupViewModel()
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bonsaiId = adapter.getBonsaiId(position)
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Eliminar Bonsai")
                    setMessage("¿Estás seguro de que deseas eliminar este Bonsai?")
                    setPositiveButton("Si") { _, _ ->
                        viewModel.deleteBonsai(bonsaiId.id)
                        adapter.removeItem(position)
                    }
                    setNegativeButton("No") { _, _ -> }
                    create()
                    show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvBonsais)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.bonsaiList.collect { bonsaiList ->
                Log.d(
                    "BonsaiListFragment",
                    "Updating RecyclerView with bonsai list of size: ${bonsaiList.size}"
                )
                adapter.updateList(bonsaiList)
                updateVisibility(bonsaiList.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupViewModel() {
        viewModel.getBonsaiList()
    }

    private fun updateVisibility(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.ivBonsai.visibility = View.VISIBLE
            binding.tvTextListBonsai.visibility = View.VISIBLE
        } else {
            binding.ivBonsai.visibility = View.GONE
            binding.tvTextListBonsai.visibility = View.GONE
        }
    }

    private fun setOnClickListeners() {
        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_bonsaiListFragment_to_addBonsaiFragment)
        }

        binding.ivExit.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_exit_title))
            .setMessage(getString(R.string.confirm_exit_message))
            .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                findNavController().navigate(R.id.action_bonsaiListFragment_to_initFragment)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupRecyclerView() {
        adapter = BonsaiAdapter(emptyList()) { bonsai ->
            val action =
                BonsaiListFragmentDirections.actionBonsaiListFragmentToEditBonsaiFragment(bonsai)
            findNavController().navigate(action)
        }
        binding.rvBonsais.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBonsais.adapter = adapter
    }


    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}