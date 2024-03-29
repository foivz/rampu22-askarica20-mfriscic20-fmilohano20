package com.example.buketomat.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buketomat.MainActivity
import com.example.buketomat.R
import com.example.buketomat.ShoppingCartActivity
import com.example.buketomat.adapters.BouquetsAdapter
import com.example.buketomat.adapters.FlowerAdapter
import com.example.buketomat.adapters.OrdersAdapter
import com.example.buketomat.backgroundworkers.BouquetsSync
import com.example.buketomat.backgroundworkers.FlowersSync
import com.example.buketomat.backgroundworkers.NetworkService
import com.example.buketomat.entites.User
import com.example.buketomat.models.*
import java.util.jar.Attributes.Name

class NewBouquetFragment : Fragment(), FlowersSync, BouquetsSync {

    // za random buket
    private lateinit var rvRandomFinishedBouquet: RecyclerView

    // za unos novog buketa
    private lateinit var rvFlowers : RecyclerView
    lateinit var btnDodajAutomatski : Button
    lateinit var btnNapraviBuket : Button
    lateinit var etOpisBuketa : EditText

    val listaCvijecaUBuketu = mutableListOf<FlowerBouquet>() // ima i atribut kolicina

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_bouquet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnDodajAutomatski = view.findViewById(R.id.btnDodajAutomatski)
        btnNapraviBuket = view.findViewById(R.id.btnNapraviBuket)
        etOpisBuketa = view.findViewById(R.id.etOpisBuketa)

        btnDodajAutomatski.setOnClickListener{
            val randomBroj = (1..10).random()
            // Toast.makeText(context, "Random buket ID: " + randomBroj.toString(), Toast.LENGTH_SHORT).show()
            // za random buket
            NetworkService.getBouquetById(randomBroj,this, requireContext())
        }

        btnNapraviBuket.setOnClickListener {
            var total = 0.0
            var nazivBuketa = "Buket ";
            var opisBuketa = "Custom buket, cvijece: ";
            val itemCount = rvFlowers.adapter?.itemCount
            for (i in 0 until itemCount!!) {

                // prolazi kroz sve i vraca viewholder za item na toj poziciji
                val holder = rvFlowers.findViewHolderForAdapterPosition(i)
                if (holder != null) {
                    val flowerNameView = holder.itemView.findViewById<View>(R.id.tv_flower_name) as TextView
                    val flowerPriceView = holder.itemView.findViewById<View>(R.id.tv_flower_price) as TextView
                    val flowerKolicinaView = holder.itemView.findViewById<View>(R.id.etKolicina) as TextView
                    val flowerId = i+1

                    // dohvati samo kojima je kolicina promijenjena
                    if (flowerKolicinaView.text.toString().toInt() > 0 ) {
                        //Toast.makeText(context, flowerPriceView.text.toString() +" Ime: " + flowerNameView.text.toString() + " id: " + flowerId + " kolicina: " + flowerKolicinaView.text.toString(), Toast.LENGTH_SHORT).show()
                        nazivBuketa += flowerNameView.text.toString() + " ";
                        opisBuketa += flowerKolicinaView.text.toString() + " " + flowerNameView.text.toString() + " ";

                        // racunanje ukupne cijene buketa
                        total += flowerKolicinaView.text.toString().toDouble() * flowerPriceView.text.toString().toDouble();
                       // Toast.makeText(context, total.toString(), Toast.LENGTH_SHORT).show()
                        Toast.makeText(context, "Uspješno dodan!", Toast.LENGTH_SHORT).show()

                        var cvijet = Flower(
                            Id = flowerId,
                            Name = flowerNameView.text.toString(),
                            Price = flowerPriceView.text.toString().toDouble()
                        )

                        var cvijetBuket = FlowerBouquet( // objekt za buket_cvijet tablicu
                            cvijet,
                            flowerKolicinaView.text.toString().toInt()
                        )
                        listaCvijecaUBuketu.add(cvijetBuket);
                    }
                }
            }
            // dodaje se jos korisnikov opis
            opisBuketa += etOpisBuketa.text.toString()

            // dodaje se buket u bazu
            NetworkService.addBouquet(total, opisBuketa, nazivBuketa, this, requireContext()) // dodavanje u tablicu buket
        }
    }

    override fun onResume() {
        super.onResume()
        NetworkService.getFlowers(this,requireContext())
    }

    // za unos novog buketa
    override fun AddFlowersToList(result: MutableList<Flower>) {
        rvFlowers = requireView().findViewById(R.id.rvFlowers)
        val flowerAdapter = FlowerAdapter(result as ArrayList<Flower>)
        rvFlowers.layoutManager = LinearLayoutManager(requireView().context)
        rvFlowers.adapter = flowerAdapter
    }

    // za random buket
    override fun AddBouquetsToList(result: MutableList<Bouquet>) {
        rvRandomFinishedBouquet = requireView().findViewById(R.id.rvRandomFinishedBouquet)
        val bouquetAdapter = BouquetsAdapter(result as ArrayList<Bouquet>,activity as MainActivity)
        rvRandomFinishedBouquet.layoutManager = LinearLayoutManager(requireView().context)
        rvRandomFinishedBouquet.adapter = bouquetAdapter
    }

    override fun onBouquetAdded(orderId: Int) {
         listaCvijecaUBuketu.forEach { item ->
              NetworkService.addBouquetItem(item,orderId,this, requireContext()); // dodaje se svaki cvijet u buket_cvijet
         }
    }

    var counter : Int = 0;
    override fun onBouquetItemAdded() {
        counter++
        if(counter == listaCvijecaUBuketu.size)
        {
            // uspjesan unos svega
            counter = 0;
        }
    }

}