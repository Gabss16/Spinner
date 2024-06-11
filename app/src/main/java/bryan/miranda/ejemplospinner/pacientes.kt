package bryan.miranda.ejemplospinner

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.dataClassDoctores
import java.util.Calendar
import java.util.UUID

class pacientes : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_pacientes, container, false)



        val spDoctores = root.findViewById<Spinner>(R.id.spDoctores)
        val txtNombrepaciente = root.findViewById<EditText>(R.id.txtNombrePaciente)
        val txtFecha = root.findViewById<EditText>(R.id.txtFechaNacimiento)
        val txtDireccionP = root.findViewById<EditText>(R.id.txtDireccionPaciente)
        val btnGuardarPaciente = root.findViewById<Button>(R.id.btnGuardarPaciente)

        //Mostrar el calendario al hacer click en el EditText txtFechaNacimientoPaciente
        txtFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
                    val fechaSeleccionada =
                        "$diaSeleccionado/${mesSeleccionado + 1}/$anioSeleccionado"
                    txtFecha.setText(fechaSeleccionada)
                },
                anio, mes, dia
            )
            datePickerDialog.show()
        }



        //1- creamos la funcion para hacer select

        fun obtenerDoctores(): List<dataClassDoctores> {

            val objConexion = ClaseConexion().cadenaConexion()
            val statement = objConexion?.createStatement()
            val resultSet = statement?.executeQuery("select * from tbDoctoress")!!

            val listaDoctores = mutableListOf<dataClassDoctores>()

            while (resultSet.next()){
                val uuid = resultSet.getString("DoctorUUID")
                val nombre = resultSet.getString("nombreDoctor")
                val especialidad = resultSet.getString("especialidad")
                val telefono = resultSet.getString("telefono")

                val doctorCompleto = dataClassDoctores(uuid, nombre, especialidad, telefono)
                listaDoctores.add(doctorCompleto)
            }
            return listaDoctores

        }

        CoroutineScope(Dispatchers.IO).launch {
            val listadoDoctores = obtenerDoctores()
            val nombreDoctores = listadoDoctores.map { it.nombreDoctor }
            withContext(Dispatchers.Main){
                val adapatador = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombreDoctores)
                spDoctores.adapter = adapatador
            }
        }

        btnGuardarPaciente.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                //Guardar datos
                val doctores= obtenerDoctores()
                val doctorId = doctores[spDoctores.selectedItemPosition].DoctorUUID
                //1- Creo un objeto de la clase conexion
                val claseC = ClaseConexion().cadenaConexion()

                //2- creo una variable que contenga un PrepareStatement
                val addProducto =
                    claseC?.prepareStatement("insert into tbPacientess(PacienteUUID, DoctorUUID, Nombre, FechaNacimiento, Direccion) values(?, ?, ?, ?, ?)")!!
                addProducto.setString(1, UUID.randomUUID().toString())
                addProducto.setString(2, doctorId)
                addProducto.setString(3, txtNombrepaciente.text.toString())
                addProducto.setString(4, txtFecha.text.toString())
                addProducto.setString(5, txtDireccionP.text.toString())
                addProducto.executeUpdate()

                //Abro una corrutina para mostrar una alerta y limpiar los campos
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                    txtNombrepaciente.setText("")
                    txtFecha.setText("")
                    txtDireccionP.setText("")
                }
            }
        }

        return root
    }
}